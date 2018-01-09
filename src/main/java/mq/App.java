package mq;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
         try{
             ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
             JmsTemplate jmsTemplate = (JmsTemplate)ctx.getBean("inputJmsTemplate");

             if(jmsTemplate != null)
                 System.out.println("Jms template is not null");
             else {
                 System.out.println("Jms template is null");
                 return;
             }

             ExecutorService executorService = Executors.newFixedThreadPool(100);

             for(int i = 0; i<100; i++){
                 executorService.submit(new MessageProducer(jmsTemplate));
             }

            executorService.shutdown();

         }catch(Exception ex){
             ex.printStackTrace();
         }
    }
}
