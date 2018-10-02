package avald.com.wallcontrol;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothConfiguration;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothWriter;
import com.github.douglasjunior.bluetoothlowenergylibrary.BluetoothLeService;

import java.lang.reflect.Method;
import java.util.UUID;

public class BLEManager {
    BluetoothConfiguration config = new BluetoothConfiguration();
    BluetoothService service;
    BluetoothDevice mDevice = null;
    BluetoothWriter writer;

    public BLEManager(Context context){
        config.context = context;
        config.bluetoothServiceClass = BluetoothLeService .class; // BluetoothClassicService.class or BluetoothLeService.class
        config.bufferSize = 1024;
        config.characterDelimiter = '\n';
        config.deviceName = "WallControl";
        config.callListenersInMainThread = true;

        // Bluetooth LE
        config.uuidService = UUID.fromString("12345678-1234-5678-1234-56789abc0010");
        config.uuidCharacteristic = UUID.fromString("12345678-1234-5678-1234-56789abc0000");

        BluetoothService.init(config);
        service = BluetoothService.getDefaultInstance();

        service.setOnScanCallback(new BluetoothService.OnBluetoothScanCallback() {
            @Override
            public void onDeviceDiscovered(BluetoothDevice device, int rssi) {
                //System.out.println(mDevice);
                if(mDevice == null && device.getName().equals("MoonBoard")){
                    mDevice = device;
                    System.out.println("FOUND D");

                    service.connect(mDevice);

                    stopScan();
                }

            }

            @Override
            public void onStartScan() {
                MainActivity.conn_stat.setText("Searching...");

                System.out.println("START");
            }

            @Override
            public void onStopScan() {

                System.out.println("END");
                if(mDevice == null)
                    MainActivity.conn_stat.setText("Disconnected");
                else{
                    MainActivity.conn_stat.setText("Connected");
                }
            }
        });

        service.setOnEventCallback(new BluetoothService.OnBluetoothEventCallback() {
            @Override
            public void onDataRead(byte[] buffer, int length) {
            }

            @Override
            public void onStatusChange(BluetoothStatus status) {
                System.out.println("status " + status);
                if(status.equals(BluetoothStatus.CONNECTED))
                    MainActivity.conn_stat.setText("Connected");
                else if(status.equals(BluetoothStatus.NONE)) {
                    mDevice = null;
                    MainActivity.conn_stat.setText("Disconnected");
                }
            }

            @Override
            public void onDeviceName(String deviceName) {
            }

            @Override
            public void onToast(String message) {
            }

            @Override
            public void onDataWrite(byte[] buffer) {
            }
        });

        writer = new BluetoothWriter(service);
    }

    public void startScan(){
        service.startScan();
    }

    public void stopScan(){
        service.stopScan();
    }

    public void disconnect(){
        service.disconnect();
    }

    public void write(int pos, String hex){
        writer.write(pos + ";#" + hex.substring(2));
    }

}
