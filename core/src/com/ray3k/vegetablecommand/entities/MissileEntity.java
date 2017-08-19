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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
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

public class MissileEntity extends Entity implements Bboxable {
    private Skeleton skeleton;
    private AnimationState animationState;
    private SkeletonBounds skeletonBounds;
    private GameState gameState;
    private boolean scored;
    private float originX;
    private float originY;
    private float targetX;
    private float targetY;
    private float missileSpeed;
    private static final Vector2 temp1 = new Vector2();
    private static final Vector2 temp2 = new Vector2();
    private boolean destroyable;
    private Team team;
    private TextureRegion textureRegion;

    public MissileEntity(GameState gameState, float originX, float originY, float targetX, float targetY, float speed) {
        super(gameState.getEntityManager(), gameState.getCore());
        this.gameState = gameState;
        
        Array<String> names = getCore().getImagePacks().get(Core.DATA_PATH + "/vegetables");
        textureRegion = getCore().getAtlas().findRegion(names.random());
        
        SkeletonData skeletonData = getCore().getAssetManager().get(Core.DATA_PATH + "/spine/line.json", SkeletonData.class);
        skeleton = new Skeleton(skeletonData);
        AnimationStateData animationStateData = new AnimationStateData(skeletonData);
        animationStateData.setDefaultMix(.25f);
        animationState = new AnimationState(animationStateData);
        animationState.setAnimation(0, "animation", true);

        skeletonBounds = new SkeletonBounds();
        skeletonBounds.update(skeleton, true);
        scored = false;
        
        this.originX = originX;
        this.originY = originY;
        
        this.targetX = targetX;
        this.targetY = targetY;
        
        this.missileSpeed = speed;
        
        setPosition(originX, originY);
        setDepth(100);
        destroyable = true;
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
        
        temp1.set(originX, originY);
        temp1.sub(getX(), getY());
        
        skeleton.findBone("line").setRotation(temp1.angle());
        skeleton.findBone("line").setScaleX(temp1.len());
        
        moveTowardsPoint(targetX, targetY, missileSpeed, delta);
        if (MathUtils.isEqual(getX(), targetX) && MathUtils.isEqual(getY(), targetY)) {
            dispose();
            ExplosionEntity exp = new ExplosionEntity(gameState);
            exp.setPosition(getX(), getY());
            exp.setTeam(team);
            gameState.playExplosionSound();
        }
    }

    @Override
    public void act_end(float delta) {

    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        getCore().getSkeletonRenderer().draw(spriteBatch, skeleton);
        spriteBatch.draw(textureRegion, getX() - textureRegion.getRegionWidth() / 2.0f, getY() - textureRegion.getRegionHeight() / 2.0f, textureRegion.getRegionWidth() / 2.0f, textureRegion.getRegionHeight() / 2.0f, textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), 1.0f, 1.0f, getRotation());
    }

    @Override
    public void destroy() {
    }

    @Override
    public void collision(Entity other) {
    }
    
    public void moveTowardsPoint(float x, float y, float speed, float delta) {
        float originalX = getX();
        float originalY = getY();
        
        temp1.set(getX(), getY());
        temp2.set(x, y);
        temp2.sub(temp1).nor();
        
        temp1.set(speed * delta, 0);
        temp1.rotate(temp2.angle());
        
        if (getX() != x) {
            addX(temp1.x);
        }
        if (getY() != y) {
            addY(temp1.y);
        }
        
        if (originalX < x && getX() > x || originalX > x && getX() < x) {
            setX(x);
        }
        
        if (originalY < y && getY() > y || originalY > y && getY() < y) {
            setY(y);
        }
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public boolean isDestroyable() {
        return destroyable;
    }

    public void setDestroyable(boolean destroyable) {
        this.destroyable = destroyable;
    }

    public SkeletonBounds getSkeletonBounds() {
        return skeletonBounds;
    }

    public Color getColor() {
        return skeleton.getColor();
    }

    public void setColor(Color color) {
        skeleton.setColor(color);
    }
}
