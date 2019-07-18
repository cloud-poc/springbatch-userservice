package org.akj.batch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import org.akj.batch.entity.Person;
import org.junit.jupiter.api.Test;

class TestDataGenerator {
	private static final String FILE_NAME = "sample-data.csv";

	private String[] firstNames = new String[] { "John", "Catherine", "Dez", "Clair", "Iris", "Joe", "Michael", "Anna",
			"Kaka", "Vic", "Wilson", "William" };

	private String[] lastNames = new String[] { "KING", "ZHANG", "HE", "WONG", "LU", "LEE", "SUN", "WU", "YANG", "ZHOU",
			"DENG", "CHEN" };

	// error rate percentage
	private float errorRate = 5;

	@Test
	void test() throws IOException {

		SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
		try (FileOutputStream outputStream = new FileOutputStream(new File(FILE_NAME));
				FileChannel channel = outputStream.getChannel();) {
			for (int i = 0; i < 50; i++) {
				Calendar cal = new Calendar.Builder().setTimeZone(TimeZone.getDefault())
						.set(Calendar.YEAR, random(1970, 2018, 10000)).set(Calendar.DAY_OF_MONTH, random(1, 28, 100))
						.set(Calendar.MONTH, random(0, 11, 100)).build();

				Person p = Person.builder().age(LocalDate.now().getYear() - cal.get(Calendar.YEAR))
						.firstName(firstNames[random(0, firstNames.length, 100)])
						.lastName(lastNames[random(0, lastNames.length, 100)]).gender((byte) random(0, 2, 10))
						.height(random(100, 230, 1000)).weight(random(20, 100, 1000)).dateOfBirth(cal.getTime())
						.pid(UUID.randomUUID().toString()).build();

				// mock an error data
				if (i % 50 == 0) {
					p.setAge(p.getAge() * -1);
				}

				StringBuffer buffer = new StringBuffer();
				buffer.append(p.getPid()).append(",").append(p.getFirstName()).append(",").append(p.getLastName())
						.append(",").append(p.getAge()).append(",").append(p.getGender()).append(",")
						.append(dateFormater.format(p.getDateOfBirth())).append(",").append(p.getHeight()).append(",")
						.append(p.getWeight()).append("\r\n");

				ByteBuffer byteBuffer = ByteBuffer.allocate(buffer.toString().getBytes().length);
				byteBuffer.put(buffer.toString().getBytes());
				byteBuffer.flip();
				channel.write(byteBuffer);
			}
		}
	}

	private int random(int minValue, int maxValue, int scale) {
		for (;;) {
			int value = (int) (Math.random() * maxValue);

			if (value >= minValue && value < maxValue) {
				return value;
			}
		}
	}

}
