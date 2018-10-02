package avald.com.wallcontrol;

/**
 * Created by User on 3/25/2018.
 */

public class Led {
    int PosX;
    int PosY;
    String position;
    String hex;
    int color = android.graphics.Color.parseColor("#aaaaaa");

    Led(int PosX, int PosY, String hex){
        this.PosX = PosX;
        this.PosY = PosY;
        setPosition(PosX + "-" + PosY);
        this.hex = hex;
    }

    public int getPosX() {
        return PosX;
    }

    public int getPosY() {
        return PosY;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }
}
