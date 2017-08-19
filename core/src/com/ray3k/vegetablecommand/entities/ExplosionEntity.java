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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonBounds;
import com.esotericsoftware.spine.SkeletonData;
import com.ray3k.vegetablecommand.Core;
import com.ray3k.vegetablecommand.Entity;
import com.ray3k.vegetablecommand.states.GameState;
import com.ray3k.vegetablecommand.states.GameState.Team;

public class ExplosionEntity extends Entity {

    private Skeleton skeleton;
    private AnimationState animationState;
    private SkeletonBounds skeletonBounds;
    private GameState gameState;
    private Team team;
    private static final int MISSILE_SCORE = 10;
    private static final int PLANE_SCORE = 30;

    public ExplosionEntity(GameState gameState) {
        super(gameState.getEntityManager(), gameState.getCore());
        this.gameState = gameState;
        SkeletonData skeletonData = getCore().getAssetManager().get(Core.DATA_PATH + "/spine/explosion.json", SkeletonData.class);
        skeleton = new Skeleton(skeletonData);
        AnimationStateData animationStateData = new AnimationStateData(skeletonData);
        animationStateData.setDefaultMix(0.0f);
        animationState = new AnimationState(animationStateData);
        animationState.setAnimation(0, "animation", false);
        animationState.apply(skeleton);
        animationState.update(0);
        skeleton.updateWorldTransform();
        animationState.addListener(new AnimationState.AnimationStateAdapter() {
            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                super.event(entry, event);
                if (event.getData().getName().equals("kill")) {
                    ExplosionEntity.this.dispose();
                }
            }

        });

        skeletonBounds = new SkeletonBounds();
        skeletonBounds.update(skeleton, true);
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
        int multiplier = 1;

        for (Entity entity : gameState.getEntityManager().getEntities()) {
            if (entity instanceof Bboxable) {
                Bboxable bboxable = (Bboxable) entity;
                boolean canBeDestroyed = (!(entity instanceof MissileEntity) || ((MissileEntity) entity).isDestroyable());
                if (canBeDestroyed && !bboxable.getTeam().equals(getTeam())) {
                    if (skeletonBounds.aabbIntersectsSkeleton(bboxable.getSkeletonBounds())) {
                        entity.dispose();
                        
                        if (entity instanceof MissileEntity) {
                            gameState.addScore((int) (MISSILE_SCORE * gameState.getEnemyController().getDifficulty()) * multiplier);
                            multiplier++;
                        } else if (entity instanceof PlaneEntity) {
                            gameState.addScore((int) (PLANE_SCORE * gameState.getEnemyController().getDifficulty()) * multiplier);
                            multiplier++;
                        }
                    }
                }
            }
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

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
