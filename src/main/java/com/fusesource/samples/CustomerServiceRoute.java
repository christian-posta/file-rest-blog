package com.fusesource.samples;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: cposta
 * Date: 4/24/12
 * Time: 2:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class CustomerServiceRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        getContext().setTracing(true);


//        from("cxfrs:bean:rsServer")
        from("cxfrs://http://localhost:9090/route?resourceClasses=com.fusesource.samples.CustomerServiceResource")
                .setHeader(Exchange.FILE_NAME, simple("test-${body}.xml"))
//                .pollEnrich("file:src/data?noop=true", 1000, new CustomerEnricher())
                .process(new CustomerServiceProcessor())
                .log("Here is the message that was enriched: ${body}");
    }
}
