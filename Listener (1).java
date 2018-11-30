package fr.ul.duckseditor.Control;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import fr.ul.duckseditor.Model.Monde;
import fr.ul.duckseditor.Model.Object.Destructible.Barre;
import fr.ul.duckseditor.Model.Object.Destructible.Block;
import fr.ul.duckseditor.Model.Object.Destructible.Enemie;
import fr.ul.duckseditor.Model.Object.Destructible.Prisonier;

import java.util.ArrayList;

public class Listener implements InputProcessor{
    private ArrayList<Integer> codes;
    private ArrayList<Body> bodyHit;
    private Camera camera;
    private Vector3 point;
    private Monde monde;
    private FileChooser fileChooser;
    private ArrayList<Body> hitted;

    public Listener(Camera camera, Monde monde, FileChooser fileChooser) {
        this.camera = camera;
        this.monde = monde;
        this.fileChooser = fileChooser;
        this.codes = new ArrayList();
        this.bodyHit = new ArrayList();
        this.hitted = new ArrayList();
        this.point = new Vector3();
    }

    private void update(){
        for (Integer c : codes){
            switch (c){
                case Input.Keys.ESCAPE: {
                    //System.out.println("EXIT");
                    Gdx.app.exit();
                    break;
                }
                default:
                    //System.out.println(c);
            }
        }
    }


    @Override
    public boolean keyDown(int keycode) {
        codes.add(Integer.valueOf(keycode));
        update();
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        //System.out.println(this.monde.getWorld().getBodyCount());


        point.set(this.camera.unproject(new Vector3(screenX, screenY, 0)));
        //this.angleInit = (float)Math.atan2(point.y, point.x);

        this.bodyHit = new ArrayList();
        this.monde.getWorld().QueryAABB(callback, (float) (point.x - 0.001), (float) (point.y - 0.001), (float)(point.x + 0.001), (float)(point.y + 0.001));
        this.hitted = new ArrayList<>(this.bodyHit);
        if(this.bodyHit.size() > 0) {
            for (Body body : this.bodyHit){
                if(this.monde.getListButton().containsKey(body)){
                    this.hitted.remove(body);
                    switch (this.monde.getListButton().get(body)){
                        case "Trash":
                            //System.out.println("Button Trash");
                            break;
                        case "Prisonier":
                            //System.out.println("Button Hero");
                            this.hitted.add(new Prisonier(monde, body.getPosition().x, body.getPosition().y, 1).getBody());
                            break;
                        case "Enemie":
                            //System.out.println("Button Enemie");
                            this.hitted.add(new Enemie(monde, body.getPosition().x, body.getPosition().y, 1).getBody());
                            break;
                        case "Block":
                            //System.out.println("Button Block");
                            this.hitted.add(new Block(monde, body.getPosition().x, body.getPosition().y, 2).getBody());
                            break;
                        case "Barre":
                            //System.out.println("Button Barre");
                            this.hitted.add(new Barre(monde, body.getPosition().x, body.getPosition().y, 4, 2).getBody());
                            break;
                        case "Save":
                            monde.saveNew();
                            //System.out.println("Button Save");
                            break;
                        case "Overide":
                            monde.saveCurent();
                            //System.out.println("Button Overide");
                            break;
                        case "Load":
                            monde.load();
                            //System.out.println("Button Load");
                            break;
                        case "Play":
                            //System.out.println("Button Play");
                            monde.mondeActif = !(monde.mondeActif);
                            break;
                        default:
                            break;
                    }
                }
                //body.applyAngularImpulse(10f, false);
                //body.applyAngularImpulse(10f, true);
                //body.applyForceToCenter(new Vector2(0,10000), true);
                //System.out.println(body);

            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        point.set(this.camera.unproject(new Vector3(screenX, screenY, 0)));

        this.monde.getWorld().QueryAABB(callback, (float) (point.x - 0.001), (float) (point.y - 0.001), (float)(point.x + 0.001), (float)(point.y + 0.001));
        if(this.bodyHit.size() > 0) {
            for (Body body : this.bodyHit){
                if(this.monde.getListButton().containsKey(body)){
                    switch (this.monde.getListButton().get(body)){
                        case "Trash":
                            for (Body body1 : this.hitted){
                                monde.delObject(body1);
                                monde.getWorld().destroyBody(body1);
                            }
                            return true;
                        default:
                            break;
                    }
                }
            }
        }
        for (Body body : this.hitted) {
            body.setAwake(true);
            body.setActive(true);
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        switch (Gdx.app.getType()){
            case Android:
            {
                if (Gdx.input.isTouched(0)){
                    //System.out.println(new Vector3(screenX, screenY, 0));
                    Vector3 delta = this.camera.unproject(new Vector3(screenX, screenY, 0)).sub(this.point);
                    //System.out.println(this.hitted);
                    for (Body body : this.hitted){
                        body.setAwake(false);
                        body.setActive(false);
                        Vector3 newPos = new Vector3(body.getPosition().x , body.getPosition().y, 0).add(delta);
                        body.setTransform(newPos.x, newPos.y, body.getAngle());
                    }
                    this.point.set(this.camera.unproject(new Vector3(screenX, screenY, 0)));
                }
                if (Gdx.input.isTouched(1)){
                    Vector3 delta = this.camera.unproject(new Vector3(screenX, screenY, 0)).sub(this.point);
                    for (Body body : this.hitted){
                        Vector2 posObjet = new Vector2(body.getPosition().x, body.getPosition().y);
                        Vector2 firstVect = new Vector2(point.x - posObjet.x, point.y - posObjet.y);
                        Vector2 secondVect = new Vector2(delta.x - posObjet.x, delta.y - posObjet.y);
                        float rotation = firstVect.angleRad(secondVect);
                        body.setTransform(posObjet.x, posObjet.y, body.getAngle()+rotation);
                        /**Vector3 delta = this.camera.unproject(new Vector3(screenX, screenY, 0)).
                                sub(new Vector3(body.getPosition().x, body.getPosition().y, 0));
                        delta.nor();
                        Vector3 init = new Vector3(point.x-body.getPosition().x, point.y-body.getPosition().y, 0);
                        init.nor();
                        // faire perpendiculaire a init
                        // produit scalaire entre deltapremier et la perpendiculaire
                        //si + alors normal
                        float ps = init.dot(delta);
                        float angle = (float) Math.acos(ps);// si - inverser angle
                        body.setAwake(false);
                        body.setActive(false);
                        Vector3 newPos = new Vector3(body.getPosition().x, body.getPosition().y,
                                (float) Math.atan2(delta.y, delta.x));
                        body.setTransform(newPos.x, newPos.y, this.angleInit.get(id) + angle);
                        id++;**/
                    }
                }
                break;
            }
            case Desktop:
            {
                if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
                    //System.out.println(new Vector3(screenX, screenY, 0));
                    Vector3 delta = this.camera.unproject(new Vector3(screenX, screenY, 0)).sub(this.point);
                    //System.out.println(this.hitted);
                    for (Body body : this.hitted){
                        body.setAwake(false);
                        body.setActive(false);
                        Vector3 newPos = new Vector3(body.getPosition().x , body.getPosition().y, 0).add(delta);
                        body.setTransform(newPos.x, newPos.y, body.getAngle());
                    }
                    this.point.set(this.camera.unproject(new Vector3(screenX, screenY, 0)));
                }
                if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)){
                    Vector3 delta = this.camera.unproject(new Vector3(screenX, screenY, 0));
                    for (Body body : this.hitted){
                        Vector2 posObjet = new Vector2(body.getPosition().x, body.getPosition().y);
                        Vector2 firstVect = new Vector2(point.x - posObjet.x, point.y - posObjet.y);
                        Vector2 secondVect = new Vector2(delta.x - posObjet.x, delta.y - posObjet.y);
                        float rotation = firstVect.angleRad(secondVect);
                        body.setTransform(posObjet.x, posObjet.y, body.getAngle()+rotation);
                        /**Vector3 delta = this.camera.unproject(new Vector3(screenX, screenY, 0)).
                                sub(new Vector3(body.getPosition().x, body.getPosition().y, 0));
                        delta.nor();
                        Vector3 init = new Vector3(point.x-body.getPosition().x, point.y-body.getPosition().y, 0);
                        init.nor();
                        // faire perpendiculaire a init
                        // produit scalaire entre deltapremier et la perpendiculaire
                        //si + alors normal
                        float ps = init.dot(delta);
                        float angle = (float) Math.acos(ps);// si - inverser angle
                        body.setAwake(false);
                        body.setActive(false);
                        Vector3 newPos = new Vector3(body.getPosition().x, body.getPosition().y,
                                (float) Math.atan2(delta.y, delta.x));
                        body.setTransform(newPos.x, newPos.y, this.angleInit.get(id) + angle);
                        id++;**/
                    }
                    this.point.set(this.camera.unproject(new Vector3(screenX, screenY, 0)));
                }
                break;
            }
            default:
            {
                System.out.println("SYSTEME NON RECONUE");
                break;
            }
        }
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        return true;
    }

    QueryCallback callback = new QueryCallback() {
        @Override
        public boolean reportFixture (Fixture fixture) {
            if (fixture.testPoint(point.x, point.y)) {
                bodyHit.add(fixture.getBody());
                return true;
            } else
                return true;
        }
    };
}
