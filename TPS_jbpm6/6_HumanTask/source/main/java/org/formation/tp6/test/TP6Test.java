package org.formation.tp6.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.Persistence;

import org.formation.tp6.model.Demande;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.jbpm.test.JBPMHelper;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.NodeInstanceContainer;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.utils.KieHelper;

/**
 * This is a sample file to launch a process.
 */
public class TP6Test {

	private static TaskService taskService;
	
	public static final void main(String[] args) {
		try {
			RuntimeManager manager = getRuntimeManager();        
            RuntimeEngine engine = manager.getRuntimeEngine(EmptyContext.get());

            
			// load up the knowledge base
//			KieBase kbase = readKnowledgeBase();
//			KieSession ksession = kbase.newKieSession();
//			KieRuntimeLogger logger = KieServices.Factory.get().getLoggers().newThreadedFileLogger(ksession, "test", 1000);
//			ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new LocalHTWorkItemHandler());

			 _testPetitCredit(engine);
			
			_testGrandCredit(engine);

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private static void _testPetitCredit(RuntimeEngine engine) throws InterruptedException {
		// start a new process instance
		Map<String, Object> params = new HashMap<String, Object>();
		KieSession ksession = engine.getKieSession();
		ProcessInstance procInst  = ksession.startProcess("Main", params);
		taskService = engine.getTaskService();
				
		List<TaskSummary> tasks = _getWaitingTasks("", "client");
		TaskSummary taskDemande = tasks.get(0);
		
		// Prise en charge de la t??che
		System.out.println("Le client1 prend la t??che " + taskDemande.getName() + "(" + taskDemande.getId() + ": " + taskDemande.getDescription() + ")");
		_claimTask(taskDemande,"client1","client");
		// D??marrage de la t??che
		_startTask(taskDemande,"client1");
		// Terminer la t??che
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("demande", new Demande(500));
		_completeTask(taskDemande,"client1",results);
		Thread.sleep(1000);
		
		tasks = _getWaitingTasks("", "agent");
		TaskSummary taskVerif = tasks.get(0);
		// Prise en charge de la t??che
		System.out.println("L'agent1 prend la t??che " + taskVerif.getName() + "(" + taskVerif.getId() + ": " + taskVerif.getDescription() + ")");
		_claimTask(taskVerif,"agent1","agent");
		_startTask(taskVerif,"agent1");
		// Terminer la t??che
		results = new HashMap<String, Object>();
		results.put("result", new Integer(1));
		_completeTask(taskVerif,"agent1",results);
		System.out.println(procInst.getState() == ProcessInstance.STATE_COMPLETED);

	}
	private static void _testGrandCredit(RuntimeEngine engine) throws InterruptedException {
		// start a new process instance
		
		// Prise en charge de la t??che
		
		// D??marrage de la t??che
		// Terminer la t??che
		
		// Prise en charge et d??marrage de la t??che par un agent
		
		// Terminer la t??che

		// Prise en charge et d??marrage de la t??che par le client : c'est lui qui d??cide !

		// Terminer la t??che

		Thread.sleep(1000);
		System.out.println("Processus termin?? ? : " + (procInst.getState() == ProcessInstance.STATE_COMPLETED));
	}



	/**
	* T??ches en attente
	*/
	private static List<TaskSummary> _getWaitingTasks(String userId, String group) {
		
		
	}
	
	/**
	* R??clamer une t??che
	*/
	private static void _claimTask(TaskSummary task,String userId, String group) {
		
		System.out.println("Prise en charge de la t??che : "+task.getName() + " par " + userId);
	}
	
	/**
	* D??marrage d'une t??che
	*/
	private static void _startTask(TaskSummary task, String userId) {
		
		System.out.println("T??che : "+task.getName() + " d??marr??e par " + userId);
	}
	
	/**
	* Terminer une t??che
	*/
	private static void _completeTask(TaskSummary task, String userId, Map<String, Object> results) {
		System.out.println("T??che : "+task.getName() + " termin??e par " + userId + " R??sultats :"+results);

	}
	
	private static RuntimeManager getRuntimeManager() {
        // load up the knowledge base
//    	JBPMHelper.startH2Server();
    	JBPMHelper.setupDataSource();
    	Properties properties= new Properties();
        properties.setProperty("client1", "client");
        properties.setProperty("client2", "client");
        properties.setProperty("agent1", "agent");
        properties.setProperty("agent2", "agent");
        UserGroupCallback userGroupCallback = new JBossUserGroupCallbackImpl(properties);
        RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get().newDefaultBuilder()
            .userGroupCallback(userGroupCallback)
            .entityManagerFactory(Persistence.createEntityManagerFactory("tp5db"))
            .addAsset(ResourceFactory.newClassPathResource("tp6/subprocess.bpmn"), ResourceType.BPMN2)
            .addAsset(ResourceFactory.newClassPathResource("tp6/Main.bpmn"), ResourceType.BPMN2)
            .get();
        return RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment);
    }
}
