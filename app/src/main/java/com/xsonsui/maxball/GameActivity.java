package com.xsonsui.maxball;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.xsonsui.maxball.game.GameClient;
import com.xsonsui.maxball.game.GameHost;
import com.xsonsui.maxball.game.GameThread;
import com.xsonsui.maxball.model.Game;
import com.xsonsui.maxball.model.Lobby;
import com.xsonsui.maxball.nuts.NutsHostModeClient;
import com.xsonsui.maxball.nuts.NutsNormalClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class GameActivity extends Activity{

    private static final String TAG = "GameActivity";
    private GameThread gameThread;
    private Game mGame = new Game();
    private GameClient gameClient;
    private GameView gameView;
    private GameHost host;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);
        gameView = (GameView) findViewById(R.id.gameView);
        //gameView = new GameView(this);
        //setContentView(gameView);

        Bundle extras = getIntent().getExtras();
        String action = extras.getString("action");
        String sessionId = extras.getString("sessionId");
        String playerName = extras.getString("playerName");
        String playerAvatar = extras.getString("playerName");

        gameThread = new GameThread(mGame, gameView);
        gameClient = new GameClient(this, gameThread, playerName, playerAvatar, action.equals("host"));
        if (action.equals("join")) {
            Lobby lobby = (Lobby) extras.getSerializable("lobby");
            NutsNormalClient client = null;
            try {
                client = new NutsNormalClient(InetAddress.getByName(lobby.ip), lobby.port, gameClient);
                gameClient.setClient(client);
                gameView.setGameInputListener(gameClient);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            client.start();
        } else if (action.equals("host")) {
            host = new GameHost(this, gameThread);
            NutsHostModeClient client = new NutsHostModeClient(host);
            host.setClient(client);
            client.start();
        } else {
            Log.e(TAG, "what? "+action);
        }

        gameThread.start();
    }

    public void connectToLocalHost(InetAddress publicAddress, int publicPort) {
        try {
            NutsNormalClient client = new NutsNormalClient(InetAddress.getLocalHost(), publicPort, gameClient);
            gameClient.setClient(client);
            gameView.setGameInputListener(gameClient);
            client.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameThread.pauseGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameThread.resumeGame();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}