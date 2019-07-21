package org.akj.batch.processor;

import org.akj.batch.entity.Person;
import org.springframework.batch.item.ItemProcessor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PersonItemProcessor implements ItemProcessor<Person, Person> {

	@Override
	public Person process(final Person person) throws Exception {
		// person.setPid(person.getPid().substring(person.getPid().lastIndexOf("-") +
		// 1));

		log.info("processing person info :" + person);

		return person;
	}

}