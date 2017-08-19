package com.ray3k.vegetablecommand.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.DelayAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.vegetablecommand.Core;
import com.ray3k.vegetablecommand.State;

public class LoadingState extends State {
    private Stage stage;
    private Skin skin;
    private ProgressBar progressBar;
    private String nextState;
    private Table root;
    private boolean finishedLoading;
    
    public LoadingState(String nextState, Core core) {
        super(core);
        this.nextState = nextState;
    }

    public String getNextState() {
        return nextState;
    }

    public void setNextState(String nextState) {
        this.nextState = nextState;
    }
    
    @Override
    public void start() {
        finishedLoading = false;
        
        stage = new Stage(new ScreenViewport());
        
        skin = createSkin();
        
        Image image= new Image(skin, "bg");
        image.setScaling(Scaling.stretch);
        image.setFillParent(true);
        stage.addActor(image);
        
        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        progressBar = new ProgressBar(0, 1, .01f, false, skin);
        progressBar.setAnimateDuration(.1f);
        root.add(progressBar).growX().expandY().pad(20.0f);
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        stage.draw();
    }

    @Override
    public void act(float delta) {
        AssetManager assetManager = getCore().getAssetManager();
        progressBar.setValue(assetManager.getProgress());
        stage.act(delta);
        if (!finishedLoading && assetManager.update()) {
            Action changeStateAction = new Action() {
                @Override
                public boolean act(float delta) {
                    if (nextState != null) {
                        finishedLoading = true;
                        packPixmaps();
                        getCore().getStateManager().loadState(nextState);
                    }
                    return true;
                }
            };
            root.addAction(new SequenceAction(new DelayAction(1.0f), changeStateAction));
        }
    }

    @Override
    public void dispose() {
        
    }
    
    private Drawable createDrawable(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fillRectangle(0, 0, width, height);
        Texture texture = new Texture(pixmap);
        return new TextureRegionDrawable(new TextureRegion(texture));
    }
    
    private Skin createSkin() {
        Skin returnValue = new Skin();
        
        returnValue.add("bg", createDrawable(20, 20, Color.DARK_GRAY), Drawable.class);
        returnValue.add("progress-bar-back", createDrawable(20, 20, Color.BLACK), Drawable.class);
        returnValue.add("progress-bar", createDrawable(1, 20, Color.BLUE), Drawable.class);
        
        ProgressBarStyle progressBarStyle = new ProgressBarStyle();
        progressBarStyle.background = returnValue.getDrawable("progress-bar-back");
        progressBarStyle.knobBefore = returnValue.getDrawable("progress-bar");
        
        returnValue.add("default-horizontal", progressBarStyle);
        
        return returnValue;
    }
    
    private void packPixmaps() {
        for (String directory : getCore().getImagePacks().keys()) {
            for (String name : getCore().getImagePacks().get(directory)) {
                FileHandle file = Gdx.files.local(directory + "/" + name + ".png");
                getCore().getPixmapPacker().pack(file.nameWithoutExtension(), getCore().getAssetManager().get(file.path(), Pixmap.class));
            }
        }
        
        getCore().getPixmapPacker().pack("white", getCore().getAssetManager().get(Core.DATA_PATH + "/gfx/white.png", Pixmap.class));
        
        TextureAtlas atlas = getCore().getPixmapPacker().generateTextureAtlas(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, false);
        getCore().setAtlas(atlas);
    }

    @Override
    public void stop() {
        stage.dispose();
        skin.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
}
