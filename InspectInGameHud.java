import net.minecraft.client.gui.hud.InGameHud;
import java.lang.reflect.Method;
public class InspectInGameHud {
    public static void main(String[] args) {
        for (Method m : InGameHud.class.getDeclaredMethods()) {
            if (m.getName().toLowerCase().contains("render")) {
                System.out.println(m.getName() + " " + java.util.Arrays.toString(m.getParameterTypes()));
            }
        }
    }
}
