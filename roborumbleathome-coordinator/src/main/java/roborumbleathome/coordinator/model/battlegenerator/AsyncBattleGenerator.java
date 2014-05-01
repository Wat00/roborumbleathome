package roborumbleathome.coordinator.model.battlegenerator;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import roborumbleathome.coordinator.model.participant.Participant;
import roborumbleathome.coordinator.service.BattleTO;

public class AsyncBattleGenerator {

    private final BattleGenerator battleGenerator;

    private final BlockingQueue<List<Participant>> participantsQueue;

    private final ExecutorService battleGeneratorExecutorService;

    public AsyncBattleGenerator(BattleGenerator battleGenerator, ExecutorService battleGeneratorExecutorService) {
	this.battleGenerator = battleGenerator;
	participantsQueue = new LinkedBlockingQueue<List<Participant>>();
	this.battleGeneratorExecutorService = battleGeneratorExecutorService;
    }

    public void setParticipants(List<Participant> participants) {
	try {
	    participantsQueue.put(participants);
	} catch (InterruptedException e) {
	    throw new RuntimeException(e);
	}
    }

    public BattleTO generateBattle() {
	try {

	    return battleGeneratorExecutorService.submit(new Callable<BattleTO>() {
		public BattleTO call() throws InterruptedException {

		    while (battleGenerator.isParticipantsEmpty() || !participantsQueue.isEmpty()) {
			List<Participant> participants = participantsQueue.take();
			battleGenerator.setParticipants(participants);
		    }

		    return battleGenerator.generateBattle();

		}
	    }).get();

	} catch (InterruptedException e) {
	    throw new RuntimeException(e);
	} catch (ExecutionException e) {
	    throw new RuntimeException(e);
	}
    }

    public void updateBattles(final UpdateBattlesTO updateBattlesTO) {
	battleGeneratorExecutorService.execute(new Runnable() {
	    public void run() {
		battleGenerator.updateBattles(updateBattlesTO);
	    }
	});
    }

    public void addPriorityPairing(final PairingTO priorityPairingTO) {
	battleGeneratorExecutorService.execute(new Runnable() {
	    public void run() {
		battleGenerator.addPriorityPairing(priorityPairingTO);
	    }
	});
    }

}