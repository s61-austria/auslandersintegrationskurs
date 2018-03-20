package example;

import model.Car;
import model.Countries;

class Example {
    private connector.Connector connector;

    public Example() {
        connector = new connector.Connector(
                "rabbitmq",
                "rabbitmq",
                "vhost",
                "localhost"
        );

        connector.subscribeToQueue(Countries.IRELAND, Car.class, ((String message) -> {
            System.out.println(message);
            return null;
        }));
    }
}
