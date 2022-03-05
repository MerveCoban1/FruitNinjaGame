package com.example.fruitninjastarter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class Fruit {

    public static float radius=60f;  //meyvelerin yarıçapı için. Ekran boyutuna göre belirleyeceğiz.
    //regular ana karakter,
    public enum Type{
        REGULAR,EXTRA,ENEMY,LIFE
    }
    Type type;
    Vector2 pos,velocity;
    public boolean living=true; //elmaları kaçırmamızla ilgili

    Fruit(Vector2 pos,Vector2 velocity){
        this.pos=pos;
        this.velocity=velocity;
        type=Type.REGULAR;
    }
    //pozisyonların ve hızların saklanması gerekiyor. Bi sınıfta saklayacağız Vektor2
    //vector: 2 veriyi saklamak için kullanıyoruz

    //bir meyveye tıklanıldı mı tıklanılmadı mı onu anlamamız gerekiyor.
    public boolean clicked(Vector2 click){
        if (pos.dst2(click)<=radius*radius+1){
            return true;
        }
        return false;
    }
    //bir meyveyle alakalı güncel pozisyonu almak istersek
    public final Vector2 getPos(){
        return pos;
    }
    //bir meyve ekranın dışına çıktı mı çıkmadı mı
    public boolean outOfScreen(){
        return (pos.y<-2f*radius);
    }
    //update etmek: meyvenin hızını ve pozisyonunu devamlı güncellememiz gerekiyor.
    public void update(float dt){
        //pozisyon vektörünü hıza göre güncelleyeceğiz
        //velocity'nin yani hızın y ve x değerlerini değiştirmek
        velocity.y-=dt*(Gdx.graphics.getHeight()*0.2f);
        velocity.x-=dt*Math.signum(velocity.x)*5f;

        pos.mulAdd(velocity,dt);
    }

}
