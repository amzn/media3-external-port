# Amazon AndroidX Media Port

AndroidX Media is a open-source collection of libraries for implementing media use cases on
Android Devices, including local playback (via ExoPlayer), video editing (via Transformer) and media sessions. Amazon has a port of Media3 ExoPlayer that is compatible with Fire TV and other Amazon devices.The Amazon port of Media3 provides many fixes, workarounds, and other patches to make ExoPlayer work on Amazon devices.
 
This repository is a port of the Media3 ExoPlayer project for Amazon devices.

See "READ-ORIGINAL.md" for the original Media3 README.

## Amazon Port Added Functionality
- Dolby Support on Gen1 FireTV
- Fixes audio latency issues on Gen4 Tablet
- Support for disabling snapping video frame release times to VSYNC added
- Support skipping profile level checks for video added
- Added support for Toshiba Fire TV edition to switch to BT while playing Dolby 
- Framedrops are seen in selected Amazon devices
- Adds a logging mechanism to ease debugging

This functionality was added on top of [Original Media3 1.3.1](https://github.com/androidx/media/tree/1.3.1).
