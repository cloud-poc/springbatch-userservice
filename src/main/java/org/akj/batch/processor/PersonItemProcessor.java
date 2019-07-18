package org.akj.batch.processor;

import org.akj.batch.entity.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class PersonItemProcessor implements ItemProcessor<Person, Person> {

	private static final Logger log = LoggerFactory.getLogger(PersonItemProcessor.class);

	@Override
	public Person process(final Person person) throws Exception {
		// person.setPid(person.getPid().substring(person.getPid().lastIndexOf("-") +
		// 1));

		return person;
	}

}