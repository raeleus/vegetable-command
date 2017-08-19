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

package com.ray3k.vegetablecommand.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.ray3k.vegetablecommand.Entity;
import com.ray3k.vegetablecommand.states.GameState;

public class EnemyControllerEntity extends Entity {
    private GameState gameState;
    private float difficulty;
    private int missileCounter;
    private int planeCounter;
    private float levelTime;
    private float levelTimer;
    private float missileTimer;
    private float planeTimer;
    private float missileSpeed;
    private static final Vector2 temp = new Vector2();

    public EnemyControllerEntity(GameState gameState, float difficulty) {
        super(gameState.getEntityManager(), gameState.getCore());
        this.gameState = gameState;
        this.difficulty = difficulty;
        missileCounter = (int) (15 * difficulty);
        planeCounter = (int) (3 * difficulty);
        levelTime = 20.0f;
        levelTimer = levelTime;
        missileSpeed = 60.0f * difficulty;
        
        missileTimer = levelTime / missileCounter;
        planeTimer = levelTime / planeCounter;
        
        for (TurretEntity turret : gameState.getTurrets()) {
            turret.setMissileCount((int)(10 * difficulty));
        }
    }

    @Override
    public void create() {
        
    }

    @Override
    public void act(float delta) {
        if (levelTimer >= 0.0f) {
            missileTimer -= delta;
            if (missileTimer < 0) {
                missileTimer = levelTime / missileCounter;
                Array<Entity> targets = new Array<Entity>();
                targets.addAll(gameState.getCities());
                targets.addAll(gameState.getTurrets());
                Entity target = targets.random();
                if (target != null) {
                    MissileEntity missile = new MissileEntity(gameState, MathUtils.random(Gdx.graphics.getWidth()), Gdx.graphics.getHeight(), target.getX(), target.getY(), missileSpeed);
                    missile.setTeam(GameState.Team.ENEMY);
                    missile.setColor(Color.RED);
                    
                    temp.set(target.getX(), target.getY());
                    temp.sub(missile.getX(), missile.getY());
                    missile.setRotation(temp.angle());
                }
            }

            planeTimer -= delta;
            if (planeTimer < 0) {
                PlaneEntity plane = new PlaneEntity(gameState);
                plane.setPosition(0.0f, Gdx.graphics.getHeight() - 75.0f);
                planeTimer = levelTime / planeCounter;
            }

            levelTimer -= delta;
        } else {
            boolean newLevel = true;
            for (Entity entity : gameState.getEntityManager().getEntities()) {
                if (entity instanceof PlaneEntity) {
                    newLevel = false;
                    break;
                } else if (entity instanceof MissileEntity) {
                    MissileEntity missile = (MissileEntity) entity;
                    if (missile.getTeam() == GameState.Team.ENEMY) {
                        newLevel = false;
                        break;
                    }
                }
            }
            
            if (newLevel) {
                dispose();
                gameState.newLevel();
            }
        }
    }

    @Override
    public void act_end(float delta) {
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void collision(Entity other) {
    }

    public float getMissileSpeed() {
        return missileSpeed;
    }

    public float getDifficulty() {
        return difficulty;
    }

}