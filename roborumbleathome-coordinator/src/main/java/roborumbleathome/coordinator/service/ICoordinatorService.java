package roborumbleathome.coordinator.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

import roborumbleathome.coordinator.model.participant.ParticipantTO;

public interface ICoordinatorService extends Remote {

    String NAME = ICoordinatorService.class.getName();

    // void addWorker() throws RemoteException;

    BattleTO generateBattle() throws RemoteException;

    byte[] downloadRobot(ParticipantTO participant) throws RemoteException;

    void uploadResults(BattleResultsTO battleResults) throws RemoteException;
}