# SwiftAddress
**THIS APPLICATION IS NO LONGER BEING DEVELOPED.**

Feel free to fork it and continue its development if you want. Note: The apk file in the Releases page is out of date. If you want the latest version, please download android studio and build this project yourself.

In android studio , the "build" button doesn't work but the "run" button does.

If you want to generate an apk file, you should run this command:

```
./gradlew :app:assembleRelease
```

You can then sign the generated apk in the terminal using `apksigner`. This is all mentioned in thsi guide:

https://developer.android.com/studio/build/building-cmdline

If you want to know more about this app **no longer being developed**, follow this link:

https://lists.openstreetmap.org/pipermail/talk/2021-May/086565.html

**WARNING: If you decide to take surveys with this app, please, please test if it works first. Either take a small survey (e.g. a 10 minute one) and open the files in JOSM and check that everything works, or simply add some fake addresses, notes and images and see if you can open this data in JOSM. You could lose data if you don't test that this application works.**

This application is designed to be able to efficiently collect housenumbers for OSM. 

**It is still a work in progress**. 

## Missing features

* Hasn't been tested on any emulators (I couldn't get them to work). Currently the only device it has been tested on is my own phone (Pocophone F1, lineage os 17.1)
* Audio notes
* fix add note dialog showing up when long press is being consumed by one of the arrows
* Be able to request permissions more than once on startup
* Display location marker as soon as location is found by device (currently location is only shown on app startup)
* More imagery layers, nicer layout to switch between imageries (possibly custom imageries too)
* Upload directly to OSM

And probably a few more features I haven't listed here

## Information

Minimum android version is Android 5.0 (Lolipop).

To find areas with missing housenumbers, use [OSM Inspector](https://tools.geofabrik.de/osmi/?view=geometry&lon=-12.00000&lat=25.00000&zoom=3) or [qa.poole.ch](http://qa.poole.ch/) (check "has addresses" to see all the areas that you should avoid).

## Images

Most icons are provided by Google Material Icons, which are under Apache License.

The icon for an address and note on the map is provided by streetcomplete, and they are licensed under CC-BY-SA.

The icon for an image on the map (camera.xml) is provided by [Elegant Themes]( 	http://www.elegantthemes.com/blog/freebie-of-the-week/beautiful-flat-icons-for-free), however the camera icon was found on [Wikipedia](https://en.m.wikipedia.org/wiki/File:Circle-icons-camera.svg). The license of this camera icon is GNU GPL Version 2.
