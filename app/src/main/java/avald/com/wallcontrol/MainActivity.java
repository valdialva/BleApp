package avald.com.wallcontrol;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class MainActivity extends AppCompatActivity {


    static TextView conn_stat;
    static LinearLayout linearLayout;
    static ColorPickerView colorPickerView;
    static TableLayout grid;
    static Button bt_connect;
    static Button save;
    static Button load;
    static Button clear;


    static Button[] buttons = new Button[96];

    static String hexColor = "#AAAAAA";
    static int Color;


    static ArrayList<Led> matrix =  new ArrayList<>();

    private BluetoothAdapter mBluetoothAdapter;
    BLEManager bleManager;
    private final static int REQUEST_ENABLE_BT = 1;

    final Handler handler = new Handler();

    final String filename = "routes.txt";
    File path;
    File file;

    ArrayList<Led> led_on = new ArrayList<Led>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        path = getApplicationContext().getFilesDir();
        file = new File(path, filename);

        bleManager = new BLEManager(getApplicationContext());

        conn_stat = (TextView) findViewById(R.id.conn_stat);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Set colorPicker height and width
        linearLayout = (LinearLayout) findViewById(R.id.color_chooser);
        LinearLayout.LayoutParams color_chooser = new LinearLayout.LayoutParams(
                getScreenWidth() / 2,
                getScreenHeight() * 1 / 6
        );
        linearLayout.setLayoutParams(color_chooser);

        //Set ColorPicker listener and change background color in touch
        colorPickerView = (ColorPickerView) findViewById(R.id.colorPickerView);
        colorPickerView.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.color_chooser);
                linearLayout.setBackgroundColor(envelope.getColor());
                Color = envelope.getColor();
                hexColor = envelope.getHexCode();
            }
        });

        bt_connect = (Button) findViewById(R.id.btn_bt);
        bt_connect.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View view) {
                      if (bleManager.mDevice == null) {
                          bleManager.startScan();
                          handler.postDelayed(new Runnable() {
                              @Override
                              public void run() {
                                  bleManager.stopScan();
                                  if(bleManager.service.getStatus().equals(BluetoothStatus.NONE))
                                    conn_stat.setText("Disconnected");
                              }
                          }, 10000);
                      }else{
                          System.out.println("DISCONNECT");
                          bleManager.service.disconnect();                      }
                  }
              }
        );

        grid = (TableLayout) findViewById(R.id.grid);

        for (int i = 0; i < 12; i++) {
            TableRow tr = new TableRow(this);
            for (int j = 0; j < 8; j++) {
                final int index = 95 - i*8 - j;
                int pos = index;
                if(i%2 == 0){
                    pos = 95 - i*8 - (7 - j);
                }

                matrix.add(new Led(i, j, hexColor, pos, index));
                buttons[index] = new Button(this);
                buttons[index].setText(matrix.get(95 - index).Pos+"");
                buttons[index].setMinimumWidth(20);
                buttons[index].setWidth(getScreenWidth()/8);
                buttons[index].setMinimumHeight(20);
                buttons[index].setHeight(getScreenHeight()* 5/6 /12 - 17);


                Drawable mDrawable = getApplicationContext().getResources().getDrawable(R.drawable.circle_button);
                mDrawable.setColorFilter(new
                        PorterDuffColorFilter(android.graphics.Color.parseColor("#aaaaaa"),PorterDuff.Mode.MULTIPLY));
                buttons[index].setBackground(mDrawable);

                buttons[index].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Led tile = matrix.get(95 - index);
                        //Toast.makeText(getApplicationContext(), "pos: "+tile.Pos + "index: "+(95-index), Toast.LENGTH_LONG).show();
                        if (Color != tile.getColor()) {
                            tile.setColor(Color);
                            tile.setHex(hexColor);

                            Drawable mDrawable = getApplicationContext().getResources().getDrawable(R.drawable.circle_button);
                            mDrawable.setColorFilter(new
                                    PorterDuffColorFilter(Color,PorterDuff.Mode.MULTIPLY));
                            buttons[index].setBackground(mDrawable);

                            led_on.add(tile);

                            bleManager.write(tile.Pos, tile.getHex());
                        }else{
                            led_on.remove(tile);
                            tile.setColor(0);
                            tile.setHex("FF000000");

                            Drawable mDrawable = getApplicationContext().getResources().getDrawable(R.drawable.circle_button);
                            mDrawable.setColorFilter(new
                                    PorterDuffColorFilter(android.graphics.Color.parseColor("#aaaaaa"),PorterDuff.Mode.MULTIPLY));
                            buttons[index].setBackground(mDrawable);

                            bleManager.write(tile.Pos, tile.getHex());

                        }
                    }
                });

                tr.addView(buttons[index]);
            } // for
            grid.addView(tr);
        } // for

        save = (Button) findViewById(R.id.save);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String save = "";

                for (Led led: led_on) {
                    save+= led.index +";"+led.hex+"|";
                }
                save = save.substring(0, save.length()-1);
                //Toast.makeText(getApplicationContext(), save, Toast.LENGTH_LONG).show();
                try {
                    writeToFile(save);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        load = (Button) findViewById(R.id.load);

        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String leds_on = readFromFile();
                    System.out.println(leds_on);
                    Led tile;
                    String led_array[] = leds_on.split("\\|");

                    reset();

                    for(int i = 0; i < led_array.length; i++){
                        String info[] = led_array[i].split(";");
                        //System.out.println("info "+led_array[i]);
                        //System.out.println("index: " +info[0]+ " color: " +info[1].substring(2));

                        tile = matrix.get(95-Integer.parseInt(info[0]));
                        tile.setHex(info[1]);
                        tile.setColor(android.graphics.Color.parseColor("#"+info[1].substring(2)));

                        led_on.add(tile);
                        Drawable mDrawable = getApplicationContext().getResources().getDrawable(R.drawable.circle_button);
                        mDrawable.setColorFilter(new
                                PorterDuffColorFilter(tile.getColor(),PorterDuff.Mode.MULTIPLY));
                        buttons[tile.index].setBackground(mDrawable);
                        //turn leds on
                        //System.out.println(tile.Pos);

                    }

                    bleManager.write(getLedString());


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        clear = (Button) findViewById(R.id.reset);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
                bleManager.reset();
            }
        });

        checkBtEnabled();
        checkLocationPermission();

    }

    public String getLedString(){
        String led = "";
        for(Led tile: led_on){
            led+=tile.Pos+";#"+tile.getHex().substring(2)+"|";
        }
        if(led != "")
            return led.substring(0,led.length()-1);
        else
            return led;
    }

    public void reset(){
        for(Iterator<Led>  it = led_on.iterator(); it.hasNext();){
            Led LED = it.next();
            it.remove();
            LED.setColor(android.graphics.Color.parseColor("#AAAAAA"));
            LED.setHex("FF000000");

            Drawable mDrawable = getApplicationContext().getResources().getDrawable(R.drawable.circle_button);
            mDrawable.setColorFilter(new
                    PorterDuffColorFilter(LED.getColor(),PorterDuff.Mode.MULTIPLY));
            buttons[LED.index].setBackground(mDrawable);

        }
    }

    public void checkBtEnabled(){
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

    }

    /**
     * @return screen width
     */
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    /**
     * @return screen height
     */
    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    private void writeToFile(String data) throws IOException {
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            stream.write(data.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stream.close();
        }
    }

    private String readFromFile() throws IOException {

        String ret = "";
        int length = (int) file.length();

        byte[] bytes = new byte[length];

        FileInputStream in = new FileInputStream(file);
        try {
            in.read(bytes);
            ret = new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
        Toast.makeText(getApplicationContext(), ret, Toast.LENGTH_LONG).show();
        return ret;
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission")
                        .setMessage("ENABLE LOCATION?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    finish();

                }
                return;
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("DESTROY");
        bleManager.service.disconnect();
    }
}
