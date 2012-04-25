To run the sample, build with maven and run camel:run to start the endpoint.
Then point your browser or favorite HTTP tool to the endpoint:

> curl http://localhost:9090/route/customerservice/customers/123

Expected response:
<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Customer><id>123</id><name>John</name></Customer>


File REST Blog
===============
See updated blog entry here: [http://www.christianposta.com/blog/?p=229](http://www.christianposta.com/blog/?p=229)

REST endpoint for integration using Apache Camel

REST is an architectural style used for organizing resources and when applied to HTTP-based services allows building stateless, decoupled, scalable services. HTTP methods, HTTP headers, and mime-types all allow a developer to achieve the REST style. Frameworks like [Jersey][5] and [Fuse Services Framework (Apache CXF)][6] can be used to speed up the development and deployment of services trying to achieve a RESTful style, and in this blog post I'd like to discuss how to build the backend of a resource that relies on integration provided by [Fuse Mediation Router][1] also known as [Apache Camel][2].

Just as an aside, a link that I've had tucked away in the recesses of my bookmarks may be of interest for those of you wondering whether your architecture is indeed RESTful or just the same highly coupled RPC style that REST tries to alleviate. Roy Fielding, who wrote his dissertation on REST, actively asserts the notion that [hyerlinks within resource representations are a must][3] for REST styles, and even further clarifies the uncertainties around implementing REST.

The source code for this sample can be found [on my github repository][4]

[Fuse Mediation Router][1] is [FuseSource's][7] enterprise-grade, hardened version of Apache Camel that provides a comfortable DSL for describing integrations, mediations, and routing. It's free, open-source, and has an Apache License. For those unfamiliar with Mediation Router/Camel, take a look at an introduction from Jon Anstey (co-author of [Camel in Action][8])at DZone's Enterprise Integration Zone: [Apache Camel: Integration Nirvana][7].

We will be using Mediation Router to help write a simple integration between a REST endpoint and a resource files on a file system. I'll be using [camel-cxfrs][11] component to expose the REST endpoint and will be using the [camel-file][12] component to read a directory on the file system. The intention of the sample is to describe the configuration necessary to expose the REST interface with Mediation Router, integrate with a backend somehow, transform the data into an appropriate REST response, and send back the response.

To get started, let's focus on how to set up the REST endpoint. To do so, you would create a JAX-RS resource that describes the java methods that will act as REST endpoints. This sample code requires familiarity with [Java API for RESTful Web Services][9] aka JAX-RS. For those unfamiliar, here are some [great tutorials to follow along][10] that help to understand JAX-RS.

[java]
@Path("/customerservice/")
public class CustomerServiceResource {

    // NOTE: The instance member variables will not be available to the
    // Camel Exchange. They must be used as method parameters for them to
    // be made available
    @Context
    private UriInfo uriInfo;

    public CustomerServiceResource() {
    }

    @GET
    @Path("/customers/{id}/")
    @Produces("text/xml")
    public Customer getCustomer(@PathParam("id") String id) {
        return null;
    }

    @PUT
    @Path("/customers/")
    public Response updateCustomer(Customer customer) {
        return null;
    }
}
[/java]

As you can see, the annotations are the JAX-RS annotations that describe the operations, HTTP methods, and mime-types involved with the REST endpoint. Notice, the return values are all null as this class will not actually be used to handle the requests that come in to the endpoint; the Mediation Router routes will be responsible for processing and responding. Note, however, that instance members are not available to the Mediation Router exchanges, i.e., any instance members injected via the JAX-RS @Context annotations will not be available. To make them available, add them as parameters to your methods.

Declaring the CXF-RS endpoint with Mediation Router can be done one of two ways: Directly in the endpoint configuration like this:

[java]
from("cxfrs://http://localhost:9090/route?resourceClasses=com.fusesource.samples.CustomerServiceResource")
[/java]


Creating it directly in the configuration requires less xml configuration but offers limited flexibility. Another option is creating a separate bean that's responsible for the endpoint and then referencing it within the endpoint configuration:

[java]
from("cxfrs:bean:rsServer")
[/java]

The bean *rsServer* should be defined in the camel context. An example:

[xml]
<cxf:rsServer id="rsServer" address="http://localhost:9090/route"
              serviceClass="com.fusesource.samples.CustomerServiceResource"/>
[/xml]

This approach allows you to decouple the endpoint configuration and allows to be quicker and less verbose in the endpoint configuration. Both options are shown in the sample code, although the first option is used.

That's all the configuration required to expose the REST endpoint with Mediation Router. Fairly simple. The next step is to consume a file from the file system based on what comes in from the REST endpoint. The contents of the file will be returned to the client of the REST call. To do this, we use the [camel-file][12] component and enrich the Exchange with a [pollEnrich][13] call in the DSL:

[java]
.setHeader(Exchange.FILE_NAME, simple("test-${body}.xml"))
.pollEnrich("file:src/data?noop=true", 1000, new CustomerEnricher())
[/java]

We cannot use any dynamic expressions in the pollEnrich call, so we set a header that the file component understands before we do the enrichment. In this case, the body of the REST message is an identifier that can be used to template the file-system resource.

Lastly, we can attach some additional processing to the route:
[java]
.process(new CustomerServiceProcessor())
[/java]

The intent of the example, as described above, is to show how to configure the endpoint and attach it to further Mediation Router processing. Note, the Message Exchange Pattern (MEP) for the REST endpoint is InOut and expects a response. The example is not meant to be a complete end-to-end solution as that will vary depending on intended functionality. Please note above the links [to Roy's discussions][3] on what REST is and is not.

If I have left something out, or you need more clarification around the example, drop me a comment and we can discuss.

[1]: http://fusesource.com/products/enterprise-camel/
[2]: http://camel.apache.org/
[3]: http://roy.gbiv.com/untangled/2008/rest-apis-must-be-hypertext-driven
[4]: https://github.com/christian-posta/file-rest-blog
[5]: http://jersey.java.net/
[6]: http://fusesource.com/products/enterprise-cxf/
[7]: http://architects.dzone.com/articles/apache-camel-integration
[8]: http://www.amazon.com/gp/product/1935182366/ref=as_li_ss_tl?ie=UTF8&tag=christianc0aa-20&linkCode=as2&camp=1789&creative=390957&creativeASIN=1935182366
[9]: http://jcp.org/en/jsr/detail?id=311
[10]: http://www.mkyong.com/tutorials/jax-rs-tutorials/
[11]: http://fusesource.com/docs/router/2.8/component_ref/_IDU_CXFRS.html
[12]: http://fusesource.com/docs/router/2.8/component_ref/_IDU_File2.html
[13]: http://fusesource.com/docs/router/2.8/apidoc/org/apache/camel/model/ProcessorDefinition.html#pollEnrich(java.lang.String)
