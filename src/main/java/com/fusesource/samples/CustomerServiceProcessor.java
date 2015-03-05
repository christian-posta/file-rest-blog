package com.fusesource.samples;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: cposta
 * Date: 4/20/12
 * Time: 11:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class CustomerServiceProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(CustomerServiceProcessor.class);
    private Map<Long, Customer> customers = new HashMap<Long, Customer>();
    private Map<Long, Order> orders = new HashMap<Long, Order>();

    public CustomerServiceProcessor() {
        init();
    }

    final void init() {
        Customer c = new Customer();
        c.setName("John");
        c.setId(123);
        customers.put(c.getId(), c);

        Order o = new Order();
        o.setDescription("order 223");
        o.setId(223);
        orders.put(o.getId(), o);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Message inMessage = exchange.getIn();
        String operationName = inMessage.getHeader(CxfConstants.OPERATION_NAME, String.class);
        if ("getCustomer".equalsIgnoreCase(operationName)) {
            String id = inMessage.getBody(String.class);
            LOG.info("----invoking getCustomer, Customer id is: " + id);

            long idNumber = Long.parseLong(id);

            Customer c = customers.get(idNumber);

            if (c == null) {
                System.out.println("Customer is null: " + (c == null) );
                Response response = Response.status(Response.Status.BAD_REQUEST).entity("<error>could not find customer</error>").build();
                exchange.getOut().setBody(response);
            }

            exchange.getOut().setBody(c);


        }

    }
}
