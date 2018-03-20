import kotlin.Unit;
import model.Car;
import model.Countries;

import javax.crypto.ExemptionMechanism;
import java.io.Console;

public class Example {
    private Connector connector;

    public Example(){
        connector = new Connector();

        connector.subscribeToQueue(Countries.IRELAND, Car.class, ((String message) -> {
            System.out.println(message);
            return null;
        }));
    }
}
