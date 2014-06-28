The IOIOLib is a modified in the following ways:

- `ioio.lib.android.accessory`: removing com.android.usb.future to avoid compilation warnings,

- `ioio.lib.util.android`: using class references instead of passing string to bootsrap the connnection cause sometimes
to app can't find anyone at runtime, and don't use the `SocketIOIOConnectionBootstrap` nor the
`BluetoothIOIConnectionBootstrap`