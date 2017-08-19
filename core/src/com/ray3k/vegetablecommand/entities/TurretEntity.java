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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonBounds;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.attachments.PointAttachment;
import com.ray3k.vegetablecommand.Core;
import com.ray3k.vegetablecommand.Entity;
import com.ray3k.vegetablecommand.states.GameState;
import com.ray3k.vegetablecommand.states.GameState.Team;

public class TurretEntity extends Entity implements Bboxable {
    private Skeleton skeleton;
    private AnimationState animationState;
    private SkeletonBounds skeletonBounds;
    private GameState gameState;
    private static final Vector2 temp = new Vector2();
    private static final float MISSILE_SPEED = 700.0f;
    private Team team;
    private Label label;
    private int missileCount;
    
    public TurretEntity(GameState gameState) {
        super(gameState.getEntityManager(), gameState.getCore());
        this.gameState = gameState;
        SkeletonData skeletonData = getCore().getAssetManager().get(Core.DATA_PATH + "/spine/base.json", SkeletonData.class);
        skeleton = new Skeleton(skeletonData);
        AnimationStateData animationStateData = new AnimationStateData(skeletonData);
        animationStateData.setDefaultMix(.25f);
        animationState = new AnimationState(animationStateData);
        animationState.setAnimation(0, "animation", true);
        
        skeletonBounds = new SkeletonBounds();
        skeletonBounds.update(skeleton, true);
        team = Team.PLAYER;
        
        label = new Label("30", gameState.getSkin());
        gameState.getStage().addActor(label);
    }

    @Override
    public void create() {
    }

    @Override
    public void act(float delta) {
        temp.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
        temp.sub(getX(), getY());
        
        if (temp.angle() >= 5.0f && temp.angle() < 175.0f) {
            skeleton.findBone("turret").setRotation(temp.angle());
        }
        
        skeleton.setPosition(getX(), getY());
        animationState.update(delta);
        skeleton.updateWorldTransform();
        animationState.apply(skeleton);
        skeletonBounds.update(skeleton, true);
        
        label.setPosition(getX(), getY() - 25, Align.center);
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
        gameState.getTurrets().removeValue(this, true);
        if (gameState.getTurrets().size == 0) {
            new GameOverTimerEntity(gameState, 2.0f);
        }
        
        gameState.getStage().getActors().removeValue(label, true);
    }

    @Override
    public void collision(Entity other) {
    }
    
    public void fire() {
        if (missileCount > 0) {
            temp.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
            temp.sub(getX(), getY());
            float angle = temp.angle();

            PointAttachment point = (PointAttachment) skeleton.getAttachment("muzzle", "muzzle");
            point.computeWorldPosition(skeleton.findBone("turret"), temp);
            MissileEntity missile = new MissileEntity(gameState, temp.x, temp.y, Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), MISSILE_SPEED);
            missile.setRotation(angle);
            missile.setTeam(GameState.Team.PLAYER);
            missile.setDestroyable(false);
            gameState.playMissileSound();
            missileCount--;
            label.setText(Integer.toString(missileCount));
        }
    }

    @Override
    public SkeletonBounds getSkeletonBounds() {
        return skeletonBounds;
    }

    @Override
    public GameState.Team getTeam() {
        return team;
    }

    public int getMissileCount() {
        return missileCount;
    }

    public void setMissileCount(int missileCount) {
        this.missileCount = missileCount;
        label.setText(Integer.toString(missileCount));
    }
}
