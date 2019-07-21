package org.akj.batch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import org.akj.batch.entity.Person;
import org.junit.jupiter.api.Test;

class TestDataGenerator {
	private static final String FILE_NAME = "sample-data";

	private static final String FILE_NAME_SUFFIX = ".csv";

	private int pageSize = 100;

	private int totalRecords = 3000;

	private String[] firstNames = new String[] { "John", "Catherine", "Dez", "Clair", "Iris", "Joe", "Michael", "Anna",
			"Kaka", "Vic", "Wilson", "William" };

	private String[] lastNames = new String[] { "KING", "ZHANG", "HE", "WONG", "LU", "LEE", "SUN", "WU", "YANG", "ZHOU",
			"DENG", "CHEN" };

	private String outputFolder = "c:/temp/source";

	// error rate percentage
	private double errorRate = 0.05;

	@Test
	void test() throws IOException {

		SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
		int index = 1;
		List<Person> lists = new ArrayList<Person>(pageSize);
		for (int i = 1; i <= totalRecords; i++) {

			Calendar cal = new Calendar.Builder().setTimeZone(TimeZone.getDefault())
					.set(Calendar.YEAR, random(1970, 2018, 10000)).set(Calendar.DAY_OF_MONTH, random(1, 28, 100))
					.set(Calendar.MONTH, random(0, 11, 100)).build();

			Person p = new Person();
			p.setAge(LocalDate.now().getYear() - cal.get(Calendar.YEAR));
			p.setDateOfBirth(cal.getTime());
			p.setFirstName(firstNames[random(0, firstNames.length, 100)]);
			p.setGender((byte) random(0, 2, 10));
			p.setLastName(lastNames[random(0, lastNames.length, 100)]);
			p.setHeight(random(100, 230, 1000));
			p.setWeight(random(20, 100, 1000));
			p.setPid(UUID.randomUUID().toString());

			// mock an error data
			if (i % 20 == 0) {
				p.setAge(p.getAge() * -1);
			}
			lists.add(p);

			if (i % pageSize == 0) {
				try (FileOutputStream outputStream = new FileOutputStream(
						new File(outputFolder + File.separatorChar + FILE_NAME + "_" + index + FILE_NAME_SUFFIX));
						FileChannel channel = outputStream.getChannel();) {

					lists.forEach(item -> {
						StringBuffer buffer = new StringBuffer();
						buffer.append(item.getPid()).append(",").append(item.getFirstName()).append(",")
								.append(item.getLastName()).append(",").append(item.getAge()).append(",")
								.append(item.getGender()).append(",").append(dateFormater.format(item.getDateOfBirth()))
								.append(",").append(item.getHeight()).append(",").append(item.getWeight())
								.append("\r\n");

						ByteBuffer byteBuffer = ByteBuffer.allocate(buffer.toString().getBytes().length);
						byteBuffer.put(buffer.toString().getBytes());
						byteBuffer.flip();
						try {
							channel.write(byteBuffer);
						} catch (IOException e) {
							System.out.println(e.getMessage());
						}
					});
					lists = new ArrayList<Person>(pageSize);
				}
				index++;
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
