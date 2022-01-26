package com.tp3.action;

import java.util.List;

import org.jbpm.api.ExecutionService;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.model.Transition;
import org.jbpm.pvm.internal.model.ExecutionImpl;

public class GetTransitionsCommand implements Command<List<Transition>>{

	String pInstId;
	
	public GetTransitionsCommand(String pInstId) {
		this.pInstId = pInstId;
	}
	@SuppressWarnings("unchecked")
	public List<Transition> execute(Environment environment) throws Exception {
		ExecutionService executionService = (ExecutionService)environment.get(ExecutionService.class);
		ExecutionImpl execution = (ExecutionImpl)executionService.findExecutionById(pInstId);
    	return (List<Transition>)execution.getActivity().getOutgoingTransitions();

	}

}
