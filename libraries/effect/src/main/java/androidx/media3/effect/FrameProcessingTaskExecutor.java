/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.media3.effect;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import androidx.media3.common.FrameProcessingException;
import androidx.media3.common.FrameProcessor;
import androidx.media3.common.util.GlUtil;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wrapper around a single thread {@link ExecutorService} for executing {@link FrameProcessingTask}
 * instances.
 *
 * <p>The wrapper handles calling {@link
 * FrameProcessor.Listener#onFrameProcessingError(FrameProcessingException)} for errors that occur
 * during these tasks.
 *
 * <p>{@linkplain #submitWithHighPriority(FrameProcessingTask) High priority tasks} are always
 * executed before {@linkplain #submit(FrameProcessingTask) default priority tasks}. Tasks with
 * equal priority are executed in FIFO order.
 */
/* package */ final class FrameProcessingTaskExecutor {

  private final ExecutorService singleThreadExecutorService;
  private final FrameProcessor.Listener listener;
  private final ConcurrentLinkedQueue<Future<?>> futures;
  private final ConcurrentLinkedQueue<FrameProcessingTask> highPriorityTasks;
  private final AtomicBoolean shouldCancelTasks;

  /** Creates a new instance. */
  public FrameProcessingTaskExecutor(
      ExecutorService singleThreadExecutorService, FrameProcessor.Listener listener) {
    this.singleThreadExecutorService = singleThreadExecutorService;
    this.listener = listener;

    futures = new ConcurrentLinkedQueue<>();
    highPriorityTasks = new ConcurrentLinkedQueue<>();
    shouldCancelTasks = new AtomicBoolean();
  }

  /**
   * Submits the given {@link FrameProcessingTask} to be executed after all pending tasks have
   * completed.
   */
  public void submit(FrameProcessingTask task) {
    if (shouldCancelTasks.get()) {
      return;
    }
    try {
      futures.add(wrapTaskAndSubmitToExecutorService(task));
    } catch (RejectedExecutionException e) {
      if (!shouldCancelTasks.getAndSet(true)) {
        listener.onFrameProcessingError(new FrameProcessingException(e));
      }
    }
  }

  /**
   * Submits the given {@link FrameProcessingTask} to be executed after the currently running task
   * and all previously submitted high-priority tasks have completed.
   *
   * <p>Tasks that were previously {@linkplain #submit(FrameProcessingTask) submitted} without
   * high-priority and have not started executing will be executed after this task is complete.
   */
  public void submitWithHighPriority(FrameProcessingTask task) {
    if (shouldCancelTasks.get()) {
      return;
    }
    highPriorityTasks.add(task);
    // If the ExecutorService has non-started tasks, the first of these non-started tasks will run
    // the task passed to this method. Just in case there are no non-started tasks, submit another
    // task to run high-priority tasks.
    submit(() -> {});
  }

  /**
   * Cancels remaining tasks, runs the given release task, and shuts down the background thread.
   *
   * @param releaseTask A {@link FrameProcessingTask} to execute before shutting down the background
   *     thread.
   * @param releaseWaitTimeMs How long to wait for the release task to terminate, in milliseconds.
   * @throws InterruptedException If interrupted while releasing resources.
   */
  public void release(FrameProcessingTask releaseTask, long releaseWaitTimeMs)
      throws InterruptedException {
    shouldCancelTasks.getAndSet(true);
    while (!futures.isEmpty()) {
      futures.remove().cancel(/* mayInterruptIfRunning= */ false);
    }
    Future<?> releaseFuture = wrapTaskAndSubmitToExecutorService(releaseTask);
    singleThreadExecutorService.shutdown();
    try {
      if (!singleThreadExecutorService.awaitTermination(releaseWaitTimeMs, MILLISECONDS)) {
        listener.onFrameProcessingError(new FrameProcessingException("Release timed out"));
      }
      releaseFuture.get();
    } catch (ExecutionException e) {
      listener.onFrameProcessingError(new FrameProcessingException(e));
    }
  }

  private Future<?> wrapTaskAndSubmitToExecutorService(FrameProcessingTask defaultPriorityTask) {
    return singleThreadExecutorService.submit(
        () -> {
          try {
            while (!highPriorityTasks.isEmpty()) {
              highPriorityTasks.remove().run();
            }
            defaultPriorityTask.run();
            removeFinishedFutures();
          } catch (FrameProcessingException | GlUtil.GlException | RuntimeException e) {
            listener.onFrameProcessingError(FrameProcessingException.from(e));
          }
        });
  }

  private void removeFinishedFutures() {
    while (!futures.isEmpty()) {
      if (!futures.element().isDone()) {
        return;
      }
      try {
        futures.remove().get();
      } catch (ExecutionException e) {
        listener.onFrameProcessingError(new FrameProcessingException(e));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        listener.onFrameProcessingError(new FrameProcessingException(e));
      }
    }
  }
}