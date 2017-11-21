# jbake-webhook
Listener for GitHub webhook to bake a static site.

You can start the listener by running the command:


    java -jar -Dworkingdir=<WORKDIR> -Dtarget=<TARGET> webhook-listener-1.0-SNAPSHOT-jar-with-dependencies.jar

- The <WORK_DIR> is the path to your local git repository of the JBake project.
- The <TARGET_DIR> is the path to the output directory of JBake. E.g. I am using a direcotry under `/var/www` because my nginx is 
configured to serve that directory as webroot.

To call the webhook, you have to create a webhook in the GitHub repository of your JBake project. For that go in the 
repository view on the tab **Settings** and then in the section **Webhooks**.

The listener is running on port 8080 and under the path /webhook, e.g. a URL might look like 

    http://www.example.com:8080/webhook
    
For the Content-Type use `application-json` and use just the push event and enable the active checkbox.

Currently there is one little bug, that is preventing that a response is sent. This effects a message to be shown on the webhook 
in GitHub saying that the **Last  delivery was not successful. Service Timeout**.
This is no real problem, the webhook is working nevertheless.
