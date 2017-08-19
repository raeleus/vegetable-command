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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonBounds;
import com.esotericsoftware.spine.SkeletonData;
import com.ray3k.vegetablecommand.Core;
import com.ray3k.vegetablecommand.Entity;
import com.ray3k.vegetablecommand.states.GameState;
import com.ray3k.vegetablecommand.states.GameState.Team;

public class PlaneEntity extends Entity implements Bboxable {
    private Skeleton skeleton;
    private AnimationState animationState;
    private SkeletonBounds skeletonBounds;
    private GameState gameState;
    private float missileTimer;
    private static final float MISSILE_TIME = 3.0f;
    private Team team;
    private static final Vector2 temp = new Vector2();
    
    public PlaneEntity(GameState gameState) {
        super(gameState.getEntityManager(), gameState.getCore());
        this.gameState = gameState;
        SkeletonData skeletonData = getCore().getAssetManager().get(Core.DATA_PATH + "/spine/plane.json", SkeletonData.class);
        skeleton = new Skeleton(skeletonData);
        skeleton.setColor(Color.RED);
        AnimationStateData animationStateData = new AnimationStateData(skeletonData);
        animationStateData.setDefaultMix(.25f);
        animationState = new AnimationState(animationStateData);
        animationState.setAnimation(0, "animation", true);
        
        skeletonBounds = new SkeletonBounds();
        skeletonBounds.update(skeleton, true);
        setMotion(100.0f, 0.0f);
        
        missileTimer = MISSILE_TIME;
        team = Team.ENEMY;
    }

    @Override
    public void create() {
    }

    @Override
    public void act(float delta) {
        skeleton.setPosition(getX(), getY());
        animationState.update(delta);
        skeleton.updateWorldTransform();
        animationState.apply(skeleton);
        skeletonBounds.update(skeleton, true);
        
        missileTimer -= delta;
        if (missileTimer < 0) {
            missileTimer = MISSILE_TIME;
            
            Array<Entity> targets = new Array<Entity>();
            for (Entity entity : gameState.getEntityManager().getEntities()) {
                if (entity instanceof CityEntity || entity instanceof TurretEntity) {
                    targets.add(entity);
                }
            }
            Entity target = targets.random();
            
            if (target != null) {
                MissileEntity missile = new MissileEntity(gameState, getX(), getY(), target.getX(), target.getY(), gameState.getEnemyController().getMissileSpeed());
                missile.setTeam(GameState.Team.ENEMY);
                missile.setColor(Color.RED);
                
                temp.set(target.getX(), target.getY());
                temp.sub(missile.getX(), missile.getY());
                missile.setRotation(temp.angle());
            }
        }
        
        if (getX() > Gdx.graphics.getWidth()) {
            dispose();
        }
    }

    @Override
    public void act_end(float delta) {
        
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        getCore().getSkeletonRenderer().draw(spriteBatch, skeleton);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void collision(Entity other) {
    }

    @Override
    public SkeletonBounds getSkeletonBounds() {
        return skeletonBounds;
    }

    @Override
    public Team getTeam() {
        return team;
    }
}
