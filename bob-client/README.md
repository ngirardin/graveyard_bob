# IOIO Master Control

## Original developper

[Nicolas Girardin](mailto:ngirardin@gmail.com)


## Building the project

```
  $ sbt
  > run
```

## IOIO Lib

The IOIOLib has been modified and recompiled in the following ways:

- `ioio.lib.android.accessory`: removing `com.android.usb.future` to avoid compilation warnings,

- `ioio.lib.util.android`: using class references instead of passing string to bootsrap the connnection cause sometimes
to app can't find any of them at runtime, and don't use the `SocketIOIOConnectionBootstrap` nor the
`BluetoothIOIConnectionBootstrap`