package org.formation.tp6.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.formation.model.Demande;
import org.h2.tools.Server;
import org.jbpm.services.task.wih.LocalHTWorkItemHandler;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.test.util.db.DataSourceFactory;
import org.kie.test.util.db.PoolingDataSourceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a sample file to launch a process.
 */
public class TP6_Test2 extends JbpmJUnitBaseTestCase {

	private static final Logger logger = LoggerFactory.getLogger(TP6_Test2.class);

	private TaskService taskService;
	RuntimeEngine engine;
	Server server;

	public TP6_Test2() {
		super(true,true);
	}
	
	@Before
	public void initEngine() {
		LocalHTWorkItemHandler handler = new LocalHTWorkItemHandler();
		addWorkItemHandler("Human Task",handler);
		manager = createRuntimeManager("org/formation/Subprocess.bpmn","org/formation/Main.bpmn");
		handler.setRuntimeManager(manager);
		
		engine = getRuntimeEngine(EmptyContext.get());
		
		
	}
	

	@Test
	public void _testPetitCredit() throws InterruptedException {
		// start a new process instance
		KieSession ksession = engine.getKieSession();
		System.out.println(""+ksession.getWorkItemManager());
		
		System.out.println("\n\n==========STARTING PROCESS : Petit Credit =============");
		Map<String, Object> params = new HashMap<String, Object>();
		ProcessInstance procInst  = ksession.startProcess("org.formation.Main", params);
		taskService = engine.getTaskService();
			
		// Get available tasks for a client
		List<TaskSummary> tasks = _getWaitingTasks("", "client");
		TaskSummary taskDemande = tasks.get(0);
		
		// client1 claims, starts and complete a request task 
		System.out.println("Le client1 prend la t??che " + taskDemande.getName() + "(" + taskDemande.getId() + ": " + taskDemande.getDescription() + ")");
		_claimTask(taskDemande,"client1","client");
		// D??marrage de la t??che
		_startTask(taskDemande,"client1");
		// Terminer la t??che
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("demande", new Demande(500));
		_completeTask(taskDemande,"client1",results);
		Thread.sleep(1000);
		
		// Gat available tasks for the group agent
		tasks = _getWaitingTasks("", "agent");
		TaskSummary taskVerif = tasks.get(0);
		
		// agent1 claim the task, start and complete it
		System.out.println("L'agent1 prend la t??che " + taskVerif.getName() + "(" + taskVerif.getId() + ": " + taskVerif.getDescription() + ")");
		_claimTask(taskVerif,"agent1","agent");
		_startTask(taskVerif,"agent1");
		// Terminer la t??che
		results = new HashMap<String, Object>();
		results.put("result", new Integer(1));
		_completeTask(taskVerif,"agent1",results);
		
		// Check that the process has ended
		assertProcessInstanceNotActive(procInst.getId(), ksession);

	}
	@Test
	public void _testGrandCredit() throws InterruptedException {
		// start a new process instance
		KieSession ksession = engine.getKieSession();
		KieRuntimeLogger kieLogger = KieServices.Factory.get().getLoggers().newThreadedFileLogger(ksession, "tp6_grand", 1000);
		
		Map<String, Object> params = new HashMap<String, Object>();
		System.out.println("\n\n==========STARTING PROCESS : Grand Credit =============");
		ProcessInstance procInst  = ksession.startProcess("org.formation.Main", params);
		
		taskService  = engine.getTaskService();
		Thread.sleep(1000);


		// T??ches disponible pour le r??le client

		
		// Le Client1 r??clame la t??che, d??marre puis compl??te la t??che "Demander" pour un montant de  2000 ???

		Thread.sleep(2000);
		
		// Tache disponibles pour un agent

		
		// agent1 r??clame, d??marre puis compl??te "Donner une note". La note est 2

		
		Thread.sleep(2000);
		
		List<Long> taskIds = taskService.getTasksByProcessInstanceId(procInst.getId());
		for ( Long taskId : taskIds ) {
			System.out.println(""+ taskService.getTaskById(taskId));
			System.out.println(""+ taskService.getTaskContent(taskId));
		}
		
		// Tasches disponibles pour client1


		// La t??che est d??j?? assign??e au client1 (swimlane)
		// client 1 d??marre er termine la t??che "Avis client". Le r??sultat est 1

		Thread.sleep(1000);
		
		assertNodeTriggered(procInst.getId(), "Demander");
		// Process is completed
		assertProcessInstanceNotActive(procInst.getId(), ksession);
		System.out.println("==========FIN PROCESS : Grand Credit =============");
		kieLogger.close();
	}

	



	private List<TaskSummary> _getWaitingTasks(String userId, String group) {
		
		List<TaskSummary> ret = taskService.getTasksAssignedAsPotentialOwner(userId, "en-UK");
		ret.addAll(taskService.getTasksAssignedAsPotentialOwner(group, ""));
		System.out.println("T??ches en attente : "+ret);
		for ( TaskSummary t : ret ) {
			System.out.println("\t : "+t.getName());
		}
		return ret;
		
	}
	
	private void _claimTask(TaskSummary task,String userId, String group) {
		
		taskService.claim(task.getId(), userId);
		System.out.println("Prise en charge de la t??che : "+task.getName() + " par " + taskService.getTaskById(task.getId()).getTaskData().getActualOwner());
	}
	
	private void _startTask(TaskSummary task, String userId) {
		taskService.start(task.getId(), userId);	
		System.out.println("T??che : "+task.getName() + " d??marr??e par " + userId);
	}
	
	private void _completeTask(TaskSummary task, String userId, Map<String, Object> results) {
		
		taskService.complete(task.getId(), userId, results);
		
		System.out.println("T??che : "+task.getName() + " termin??e par " + userId + " R??sultats :"+results);

	}
	
	@Override
    protected PoolingDataSourceWrapper setupPoolingDataSource() {
        Properties driverProperties = new Properties();
        driverProperties.put("user", "sa");
        driverProperties.put("password", "");
        driverProperties.put("url", "jdbc:h2:tcp://localhost/~/test");
        driverProperties.put("driverClassName", "org.h2.Driver");
        driverProperties.put("className", "org.h2.jdbcx.JdbcDataSource");
        
        PoolingDataSourceWrapper pds = null;
        try {
            pds = DataSourceFactory.setupPoolingDataSource("jdbc/jbpm-ds", driverProperties);
        } catch (Exception e) {
            logger.warn("DBPOOL_MGR:Looks like there is an issue with creating db pool because of " + e.getMessage() + " cleaing up...");
            logger.debug("DBPOOL_MGR: attempting to create db pool again...");
            pds = DataSourceFactory.setupPoolingDataSource("jdbc/jbpm-ds", driverProperties);
            logger.debug("DBPOOL_MGR:Pool created after cleanup of leftover resources");
        }
        return pds;
    }
//    protected PoolingDataSourceWrapper setupPoolingDataSource() {
//        Properties driverProperties = new Properties();
//        driverProperties.put("user", "root");
//        driverProperties.put("password", "root");
//        driverProperties.put("url", "jdbc:mysql://localhost:3306/jbpm");
//        driverProperties.put("driverClassName", "com.mysql.Driver");
//        driverProperties.put("className", "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
//        driverProperties.put("databaseName", "jbpm");
//        driverProperties.put("serverName", "localhost");
//        driverProperties.put("portNumber", "3306");
//        
//        
//        PoolingDataSourceWrapper pds = null;
//        try {
//            pds = DataSourceFactory.setupPoolingDataSource("jdbc/jbpm-ds", driverProperties);
//        } catch (Exception e) {
//            logger.warn("DBPOOL_MGR:Looks like there is an issue with creating db pool because of " + e.getMessage() + " cleaing up...");
//            logger.debug("DBPOOL_MGR: attempting to create db pool again...");
//            pds = DataSourceFactory.setupPoolingDataSource("jdbc/jbpm-ds", driverProperties);
//            logger.debug("DBPOOL_MGR:Pool created after cleanup of leftover resources");
//        }
//        return pds;
//    }
	
}
