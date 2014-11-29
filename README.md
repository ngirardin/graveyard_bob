# IOIO Master Control

## Original developper

[Nicolas Girardin](mailto:ngirardin@gmail.com)


## Building the project

The `org.scaloid.scaloid-support-v4` lib need to be built locally until it's published to an external maven repo. Instruction at: https://github.com/pocorall/scaloid/issues/82.

To run the project on a device, run:

```
  $ sbt
  > run
```

When the app compiles but crash at runtime that a class is not found, running `sbt clean` can come handy.


## Openning the project in IntelliJ IDEA

```
  $ sbt
  > gen-idea
```


## IOIO Lib

The IOIOLib has been modified and recompiled in the following ways:

- `ioio.lib.android.accessory`: removing `com.android.usb.future` to avoid compilation warnings,

- `ioio.lib.util.android`: using class references instead of passing string to bootsrap the connnection cause sometimes
to app can't find any of them at runtime, and don't use the `SocketIOIOConnectionBootstrap` nor the
`BluetoothIOIConnectionBootstrap`
