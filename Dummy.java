import net.minecraft.client.gui.hud.BossBarHud;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Dummy {
    public static void main(String[] args) {
        for (Field f : BossBarHud.class.getDeclaredFields()) {
            System.out.println("F: " + f.getType().getName() + " " + f.getName());
        }
        for (Method m : BossBarHud.class.getDeclaredMethods()) {
            System.out.println("M: " + m.getName());
        }
    }
}
