package s05.t02;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AlmitaVirtualApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlmitaVirtualApplication.class, args);
	}

}
