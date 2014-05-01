package roborumbleathome.worker.integration;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import roborumbleathome.coordinator.model.participant.ParticipantTO;
import roborumbleathome.coordinator.service.BattleResultsTO;
import roborumbleathome.coordinator.service.BattleTO;
import roborumbleathome.coordinator.service.ICoordinatorService;

public class CoordinatorProxy {

    public static class CannotCommunicateWithCoordinatorProcessException extends Exception {

	public CannotCommunicateWithCoordinatorProcessException(Throwable cause) {
	    super(cause);
	}

    }

    private final ICoordinatorService coordinatorService;

    public CoordinatorProxy(String host) throws CannotCommunicateWithCoordinatorProcessException {
	try {

	    Registry registry = LocateRegistry.getRegistry(host);
	    coordinatorService = (ICoordinatorService) registry.lookup(ICoordinatorService.NAME);

	} catch (RemoteException e) {
	    throw new CannotCommunicateWithCoordinatorProcessException(e);

	} catch (NotBoundException e) {
	    throw new CannotCommunicateWithCoordinatorProcessException(e);

	}
    }

    public BattleTO generateBattle() throws CannotCommunicateWithCoordinatorProcessException {
	try {

	    return coordinatorService.generateBattle();

	} catch (RemoteException e) {
	    throw new CannotCommunicateWithCoordinatorProcessException(e);
	}
    }

    public byte[] downloadRobot(ParticipantTO participant) throws CannotCommunicateWithCoordinatorProcessException {
	try {

	    return coordinatorService.downloadRobot(participant);

	} catch (RemoteException e) {
	    throw new CannotCommunicateWithCoordinatorProcessException(e);
	}
    }

    public void uploadResults(BattleResultsTO battleResults) throws CannotCommunicateWithCoordinatorProcessException {
	try {

	    coordinatorService.uploadResults(battleResults);

	} catch (RemoteException e) {
	    throw new CannotCommunicateWithCoordinatorProcessException(e);
	}
    }

}