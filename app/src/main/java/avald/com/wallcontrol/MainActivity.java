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
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {


    static TextView conn_stat;
    static LinearLayout linearLayout;
    static ColorPickerView colorPickerView;
    static TableLayout grid;
    static Button bt_connect;

    static Button[] buttons = new Button[96];

    static String hexColor = "#AAAAAA";
    static int Color;


    static ArrayList<Led> matrix =  new ArrayList<>();

    private BluetoothAdapter mBluetoothAdapter;
    BLEManager bleManager;
    private final static int REQUEST_ENABLE_BT = 1;

    final Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

                matrix.add(new Led(i, j, hexColor));
                buttons[index] = new Button(this);
                buttons[index].setText(matrix.get(95 - index).getPosition());
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
                        Led tile = matrix.get(index);
                        if (Color != tile.getColor()) {
                            tile.setColor(Color);
                            tile.setHex(hexColor);

                            Drawable mDrawable = getApplicationContext().getResources().getDrawable(R.drawable.circle_button);
                            mDrawable.setColorFilter(new
                                    PorterDuffColorFilter(Color,PorterDuff.Mode.MULTIPLY));
                            buttons[index].setBackground(mDrawable);

                            bleManager.write(index, tile.hex);
                        }else{
                            tile.setColor(0);
                            tile.setHex("FF000000");

                            Drawable mDrawable = getApplicationContext().getResources().getDrawable(R.drawable.circle_button);
                            mDrawable.setColorFilter(new
                                    PorterDuffColorFilter(android.graphics.Color.parseColor("#aaaaaa"),PorterDuff.Mode.MULTIPLY));
                            buttons[index].setBackground(mDrawable);

                            bleManager.write(index, tile.hex);

                        }
                    }
                });

                tr.addView(buttons[index]);
            } // for
            grid.addView(tr);
        } // for

        checkBtEnabled();
        checkLocationPermission();

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
