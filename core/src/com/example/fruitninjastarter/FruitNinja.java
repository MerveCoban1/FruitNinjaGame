package com.example.fruitninjastarter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Random;

public class FruitNinja extends ApplicationAdapter implements InputProcessor {
    SpriteBatch batch;
    Texture background;
    Texture apple, bill, cherry, ruby;

    BitmapFont font;
    BitmapFont font2;
    BitmapFont font3;
    //internete freetype font generator yazarak ordan dependency kısmına eklememız gereken şeyleri buluyoruz.
    //hem android hem de core kısmı için olan kodları alıp gradle'ımıza ekliyoruz.
    FreeTypeFontGenerator fontGenerator;
    FreeTypeFontGenerator fontGenerator2;
    FreeTypeFontGenerator fontGenerator3;

    //kaç tane canımız olacak, skor ne onları takip edelim
    int lives = 0;
    int score = 0;
    int bestScore = 0;

    //zamanı takip edelim
    private double currentTime; //güncel zaman
    private double gameOverTime = -1.0f; //oyunun bittiği zaman

    //yapıcağımız yer meyveleri ekleyeceğimiz yer rastgele olacak
    Random random = new Random();
    Array<Fruit> fruitArray = new Array<Fruit>(); //meyveleri ekledikçe bu dizinin içinde tutacağız.
    //ekran dışına çıktıkça da meyve bu diziden çıkartabilirim

    //şimdi ne zamanda kaç saniyede ne kadar item oluşturacağımızı belirleyelim
    float genCounter = 0;  //kaç tane oluşturduk kontrol ediyoruz.
    private final float startGenSpeed = 1.1f;  //başlatma hızı
    float genSpeed = startGenSpeed; //gerçek başlatma hızım

    @Override
    public void create() {
        batch = new SpriteBatch();
        background = new Texture("ninjabackground.png");
        apple = new Texture("apple.png");
        bill = new Texture("bill.png");
        cherry = new Texture("cherry.png");
        ruby = new Texture("ruby.png");

        //static bir değişken olduğu için direk erişebiliyoruz.
        Fruit.radius = Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) / 20f;  //20 yi azaltırsan meyveler büyür

        //Gdx'in kullanıcının ekrana tıklamasını falan  algılaması için InputProcessor sınıfını implements ediyoruz önce
        //implemets etmem gereken tüm metotları da implement ettik.
        //ve ekliyoruz
        Gdx.input.setInputProcessor(this); //kim bu imput processing işlemini yapacak belirtiyoruz this yani.
        //artık kullanıcın burda yaptığı işlemleri anlayabiliyoruz.

        //şimdi fontlarla çalışma işlemlerine geçelim
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("robotobold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter(); //bazı parametreler ekleyebiliyoruz.
        parameter.color = Color.WHITE;
        parameter.size = 45;
        parameter.characters = "0123456789 PTScreCutoplay:.+-";  //bunları kullanabilirim diyorum.
        //ekrana yazdırdığın her kelime burda olmak zorunda yoksa yazdırmıyor. Ve büyük küçük de fark ediyor.
        font = fontGenerator.generateFont(parameter);//fontumu oluşturdum. Yani oyunumun içinde bir şey yazdırmak istediğimde bu fontu kullanabiliyorum.
        fontGenerator2 = new FreeTypeFontGenerator(Gdx.files.internal("robotobold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter2 = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter2.color = Color.WHITE;
        parameter2.size = 80;
        parameter2.characters = "0123456789 PTScreCutoplay:.+-";
        font2 = fontGenerator.generateFont(parameter2);
        fontGenerator3 = new FreeTypeFontGenerator(Gdx.files.internal("robotobold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter3 = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter3.color = Color.BLUE;
        parameter3.size = 50;
        parameter3.characters = "0123456789 BestScore:.+-";
        font3 = fontGenerator.generateFont(parameter3);

    }

    @Override
    public void render() {
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        double newTime = TimeUtils.millis() / 1000.0;
        double frameTime = Math.min(newTime - currentTime, 0.3);
        float deltaTime = (float) frameTime;
        currentTime = newTime;

        if (lives <= 0 && gameOverTime == 0f) {
            //game over
            gameOverTime = currentTime;
        }
        if (lives > 0) {
            //game mode
            //add itemi çağırıyoruz
            genSpeed -= deltaTime * 0.015f;
            if (genCounter <= 0f) {
                genCounter = genSpeed;
                addItem();
            } else {
                genCounter -= deltaTime;
            }

            //oyuncunun kaç canı olduğunu çizdirdik. Elma ile
            for (int i = 0; i < lives; i++) {
                batch.draw(apple, i * 30f + 20f, Gdx.graphics.getHeight() - 65f, 30f, 30f);
            }
            for (Fruit fruit : fruitArray) {
                fruit.update(deltaTime);

                switch (fruit.type) {
                    case LIFE:
                        batch.draw(bill, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
                        break;
                    case ENEMY:
                        batch.draw(ruby, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
                        break;
                    case REGULAR:
                        batch.draw(apple, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
                        break;
                    case EXTRA:
                        batch.draw(cherry, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
                        break;
                }
            }
            //elmalar düşünce ne olacak onu belirleyelim
            boolean holdLives = false;
            Array<Fruit> toRemove = new Array<Fruit>();
            for (Fruit fruit : fruitArray) {
                if (fruit.outOfScreen()) {
                    toRemove.add(fruit);

                    if (fruit.living && fruit.type == Fruit.Type.REGULAR) {
                        lives--;
                        holdLives = true;
                        break;
                    }
                }
            }
            if (holdLives) {
                for (Fruit f : fruitArray) {
                    f.living = false; //bi tane kaçırınca o an orda bulunan meyveler yere düşse de canım azalmıycak
                }
            }
            for (Fruit f : toRemove) {
                fruitArray.removeValue(f, true);
            }
        }

        font.draw(batch, "Score: " + score, 70, 70);
        if (lives <= 0) {
            font2.draw(batch, "Cut To Play", Gdx.graphics.getWidth() * 0.4f, Gdx.graphics.getHeight() * 0.5f);
        }
        font3.draw(batch, "Best Score: " + bestScore, Gdx.graphics.getWidth() * 0.8f, Gdx.graphics.getHeight() * 0.9f);
        batch.end();
    }

    private void addItem() {
        //pozisyonu random yapmamız lazım
        float pos = random.nextFloat() * Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth());
        Fruit item = new Fruit(new Vector2(pos, -Fruit.radius), new Vector2((Gdx.graphics.getWidth() * 0.5f - pos) * (0.3f + (random.nextFloat() - 0.5f)), (Gdx.graphics.getHeight() * 0.5f))); //pozisyon ve hız istiyor.

        //bir elma mı gösteriyorum yoksa başka bir şey mi buna karar verelim
        float type = random.nextFloat();
        if (type > 0.98) {
            item.type = Fruit.Type.LIFE;
        } else if (type > 0.88) {
            item.type = Fruit.Type.EXTRA;
        } else if (type > 0.78) {
            item.type = Fruit.Type.ENEMY;
        }//zaten 0.78in altında da zamanın çoğunda da elma gelicek öyle ayarlamıştık fruit sınıfımızda

        fruitArray.add(item);
        //itemi oluşturduk şimdi render da göstereceğiz
    }

    @Override
    public void dispose() { //kullandığımız her şeyden kurtuluyoruz burda.
        batch.dispose();
        font.dispose();
        fontGenerator.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    //kullanıcı dokundu ve sürükledi. Kullanıcının ekranda sürüklediğini bu metot ile anlıyoruz.
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (lives <= 0 && (currentTime - gameOverTime) > 2f) {
            //menu mode
            if (score > bestScore) {
                bestScore = score;
            }
            gameOverTime = 0;
            score = 0;
            lives = 4;
            genSpeed = startGenSpeed;
            fruitArray.clear();
        } else {
            //game mode
            //tıklanınca fruitleri fruitarray'den çıkartıcaz
            Array<Fruit> toRemove = new Array<Fruit>();
            Vector2 pos = new Vector2(screenX, Gdx.graphics.getHeight() - screenY);

            int plusSkor = 0;
            for (Fruit f : fruitArray) {
                if (f.clicked(pos)) {
                    toRemove.add(f);

                    switch (f.type) {
                        case EXTRA:
                            plusSkor += 2;
                            score++;
                            break;
                        case REGULAR:
                            plusSkor++;
                            break;
                        case ENEMY:
                            lives--;
                            break;
                        case LIFE:
                            lives++;
                            break;
                    }
                }
            }
            score += plusSkor * plusSkor;

            for (Fruit f : toRemove) {
                fruitArray.removeValue(f, true);
            }
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
