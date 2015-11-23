import static com.fizzed.blaze.Systems.exec;

public class blaze {

    public void demo() {
        exec("mvn", "-Pninja-run", "test")
            .run();
    }

}