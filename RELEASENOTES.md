# Release Notes

AndroidX Media is a open-source collection of libraries for implementing media use cases on Android Devices, including local playback (via ExoPlayer), video editing (via Transformer) and media sessions. Amazon has a port of Media3 that is compatible with Fire TV and other Amazon devices.The Amazon port of Media3 ExoPlayer provides many fixes, workarounds, and other patches to make ExoPlayer work on Amazon devices.


## Using Media3

### (A) From the Maven Repository
* To get started using Amazon Media3 is to add the gradle dependencies of the library you need in the build file of your app module

    ```groovy
    implementation 'com.amazom.android:media3-exoplayer:1.3.1'
    implementation 'com.amazon.android:media3-exoplayer-dash:1.3.1'
    implementation 'com.amazon.android:media3-ui:1.3.1'
    ```

* Turn on the Java8 support in all build files depending on the Amazon Media3, by adding the following command:
  
    ```groovy
    compileOptions {
      targetCompatibility JavaVersion.VERSION_1_8
    }
    ```

* Enable Multidex
If your Gradle `minSdkVersion` is 20 or lower, you should [enable multidex](https://developer.android.com/studio/build/multidex) in order to prevent build errors.


### (B) Locally

Cloning the repository and depending on the modules locally is required when using some libraries. It's also a suitable approach if you want to make local changes.

First clone the repository into a local directory:
```sh
git clone https://github.com/amzn/media3-external-port.git
```

Next, add the following to your project's settings.gradle file, replacing path/to/media with the path to your local copy 

```groovy
gradle.ext.androidxMediaModulePrefix = 'media-'
apply from: file("path/to/media/core_settings.gradle")
```

You should now see the AndroidX Media modules appear as part of your project. You can depend on them from `build.gradle` as you would on any other local module, for example:

```groovy
implementation project(':media-lib-exoplayer')
implementation project(':media-lib-exoplayer-dash')
implementation project(':media-lib-ui')
```


## Migrating from Amazon Exoplayer to Amazon Media3

### Adding Media3 Dependency in your build file
To migrate from Amazon ExoPlayer to Amazon Media3 you are required to change the dependency in your build file of your application. While adding the dependency, the group id will be same(com.amazon.android) for the Amazon media3 libraries, the change will only be in the artifacts name that you are adding into your project(for e.g exoplayer-core to media3-exoplayer).
For eg.

<code>implementation ‘com.amazon.android:exoplayer-core:2.X.X'</code>  to  <code>implementation ‘com.amazon.android:media3-exoplayer:1.3.1'</code>

For mapping the artifacts you can refer to this [doc](https://developer.android.com/media/media3/exoplayer/mappings#dependency).

### Importing Media3 classes in your code
Now for importing the Exoplayer and other instances in your code, the import statement in your code must be androidx.media3.*
<br>
For e.g. For importing the Media3 Exoplayer, the import statement in your code you have to change it from:

```
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.util.MimeTypes
```
to 
```
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
```

For complete migration guide for Media3 you can refer to the original documentation of [Google for Migration](https://developer.android.com/media/media3/exoplayer/migration-guide).
