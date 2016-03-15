package io.pivotal.enablement.articulate.controller;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.pivotal.enablement.articulate.client.model.Attendee;
import io.pivotal.enablement.articulate.service.AttendeeService;
import io.pivotal.enablement.articulate.service.EnvironmentHelper;

/**
 * AttendeeController
 * 
 * This is the MVC controller for the application. All UI HTTP requests get
 * here. We're using Thymeleaf as the template engine.
 * 
 * 
 * @author mborges
 *
 */
@Controller
public class AttendeeController {

	private Log log = LogFactory.getLog(AttendeeController.class);

	@Autowired
	private AttendeeService attendeeService;

	@Autowired
	private EnvironmentHelper environmentHelper;

	/**
	 * INDEX
	 * 
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/")
	public String index(Model model) throws Exception {
		addAppEnv(model);
		return "index";
	}

	/**
	 * BASICS
	 * 
	 * Action to initiate shutdown of the system. In CF, the application
	 * <em>should</em> restart. In other environments, the application runtime
	 * will be shut down.
	 * 
	 * @throws Exception
	 */
	@RequestMapping(value = "/basics", method = RequestMethod.GET)
	public String kill(@RequestParam(value = "doit", required = false) boolean doit, Model model) throws Exception {

		addAppEnv(model);

		if (doit) {
			model.addAttribute("killed", true);
			log.warn("*** The system is shutting down. ***");
			Runnable killTask = () -> {
				try {
					String name = Thread.currentThread().getName();
					log.warn("killing shortly " + name);
					TimeUnit.SECONDS.sleep(5);
					log.warn("killed " + name);
					System.exit(0);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
			new Thread(killTask).start();
		}

		return "basics";

	}

	/**
	 * SERVICES
	 * 
	 * @param model
	 *            The model for this action.
	 * @return The path to the view.
	 */
	@RequestMapping(value = "/services", method = RequestMethod.GET)
	public String attendees(Model model) throws Exception {

		model.addAttribute("attendees", attendeeService.getAttendees());
		
		addAppEnv(model);
		return "services";
	}

	/**
	 * SERVICES - Add Attendee
	 * 
	 * NOTE: this method chains (calls) the "attendees" method so it returns the
	 * services template with the updated attendees list.
	 * 
	 * TODO:
	 * - Turn this this into REST call
	 * 
	 * @param firstName
	 * @param lastName
	 * @param emailAddress
	 * @param model
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/add-attendee", method = RequestMethod.POST)
	public String addAttendee(@RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName,
			@RequestParam("emailAddress") String emailAddress, Model model) throws Exception {

		Attendee attendee = new Attendee();
		attendee.setFirstName(firstName);
		attendee.setLastName(lastName);
		attendee.setEmailAddress(emailAddress);

		attendeeService.add(attendee);
		model.addAttribute("attendees", attendeeService.getAttendees());

		addAppEnv(model);
		return "services";
	}

	/**
	 * BLUEGREEN
	 * 
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/bluegreen")
	public String bluegreen(Model model) throws Exception {

		for (String key : System.getenv().keySet()) {
			System.out.println(key + ":" + System.getenv(key));
		}

		addAppEnv(model);

		return "bluegreen";
	}

	///////////////////////////////////////
	// Helper Methods
	///////////////////////////////////////

	private void addAppEnv(Model model) throws Exception {

		Map<String, Object> modelMap = environmentHelper.addAppEnv();
		model.addAllAttributes(modelMap);
	}

}