package ru.iteco.attach;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.iteco.attach.property.StorageProperty;

@SpringBootApplication
@EnableConfigurationProperties({StorageProperty.class})
public class AttachApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttachApplication.class, args);
    }

}
