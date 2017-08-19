/*
 * The MIT License
 *
 * Copyright 2017 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ray3k.vegetablecommand.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ray3k.vegetablecommand.Core;
import com.ray3k.vegetablecommand.EntityManager;
import com.ray3k.vegetablecommand.InputManager;
import com.ray3k.vegetablecommand.State;
import com.ray3k.vegetablecommand.entities.BackgroundEntity;
import com.ray3k.vegetablecommand.entities.CityEntity;
import com.ray3k.vegetablecommand.entities.EnemyControllerEntity;
import com.ray3k.vegetablecommand.entities.TurretEntity;

public class GameState extends State {
    private int score;
    private static int highscore = 0;
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private OrthographicCamera uiCamera;
    private Viewport uiViewport;
    private InputManager inputManager;
    private Skin skin;
    private Stage stage;
    private Table table;
    private Label scoreLabel;
    private EntityManager entityManager;
    private Array<TurretEntity> turrets;
    private Array<CityEntity> cities;
    private int turretCounter;
    private boolean fired;
    private EnemyControllerEntity enemyController;
    private float difficulty;
    private static final float DIFFICULTY_INCREMENT = .1f;
    
    public static enum Team {
        PLAYER, ENEMY;
    }
    
    public GameState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        difficulty = 1.0f;
        fired = false;
        cities = new Array<CityEntity>();
        turrets = new Array<TurretEntity>();
        score = 0;
        
        inputManager = new InputManager(); 
        
        uiCamera = new OrthographicCamera();
        uiViewport = new ScreenViewport(uiCamera);
        uiViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiViewport.apply();
        
        uiCamera.position.set(uiCamera.viewportWidth / 2, uiCamera.viewportHeight / 2, 0);
        
        gameCamera = new OrthographicCamera();
        gameViewport = new ScreenViewport(gameCamera);
        gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gameViewport.apply();
        
        gameCamera.position.set(gameCamera.viewportWidth / 2, gameCamera.viewportHeight / 2, 0);
        
        skin = getCore().getAssetManager().get(Core.DATA_PATH + "/ui/vegetable-command-ui.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(inputManager);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        entityManager = new EntityManager();
        
        createStageElements();
        
        BackgroundEntity bgEntity = new BackgroundEntity(this);
        bgEntity.setPosition(0.0f, 0.0f);
        
        spawnPlayer();
        newLevel();
    }
    
    private void createStageElements() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        scoreLabel = new Label("0", skin);
        root.add(scoreLabel).expandY().padTop(25.0f).top();
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        gameCamera.update();
        spriteBatch.setProjectionMatrix(gameCamera.combined);
        spriteBatch.begin();
        entityManager.draw(spriteBatch, delta);
        spriteBatch.end();
        
        stage.draw();
    }

    @Override
    public void act(float delta) {
        entityManager.act(delta);
        
        stage.act(delta);
        
        if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
            if (!fired && turrets.size > 0) {
                turretCounter %= turrets.size;
                turrets.get(turretCounter).fire();
                turretCounter++;
                fired = true;
            }
        } else {
            fired = false;
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void stop() {
        stage.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
        gameCamera.position.set(width / 2, height / 2.0f, 0.0f);
        
        uiViewport.update(width, height);
        uiCamera.position.set(uiCamera.viewportWidth / 2, uiCamera.viewportHeight / 2, 0);
        stage.getViewport().update(width, height, true);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        scoreLabel.setText(Integer.toString(score));
        if (score > highscore) {
            highscore = score;
        }
    }
    
    public void addScore(int score) {
        this.score += score;
        scoreLabel.setText(Integer.toString(this.score));
        if (this.score > highscore) {
            highscore = this.score;
        }
    }
    
    public void playExplosionSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/explosion.wav", Sound.class).play(.5f);
    }
    
    public void playCitySound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/city.wav", Sound.class).play(.5f);
    }
    
    public void playMissileSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/missile.wav", Sound.class).play(.5f);
    }

    public OrthographicCamera getGameCamera() {
        return gameCamera;
    }

    public void setGameCamera(OrthographicCamera gameCamera) {
        this.gameCamera = gameCamera;
    }
    
    public void spawnPlayer() {
        float border = 40.0f;
        
        boolean spawnCity = true;
        
        for (int i = 0; i < 7; i++) {
            float x = border + i * (Gdx.graphics.getWidth() - border * 2) / 6;
            if (spawnCity) {
                CityEntity city = new CityEntity(this);
                city.setPosition(x, 110.0f);
                cities.add(city);
            } else {
                TurretEntity turret = new TurretEntity(this);
                turret.setPosition(x, 110.0f);
                turrets.add(turret);
            }
            spawnCity = !spawnCity;
        }
    }

    public Array<TurretEntity> getTurrets() {
        return turrets;
    }

    public Array<CityEntity> getCities() {
        return cities;
    }

    public EnemyControllerEntity getEnemyController() {
        return enemyController;
    }
    
    public void newLevel() {
        enemyController = new EnemyControllerEntity(this, difficulty);
        difficulty += DIFFICULTY_INCREMENT;
    }

    public Skin getSkin() {
        return skin;
    }

    public Stage getStage() {
        return stage;
    }
}