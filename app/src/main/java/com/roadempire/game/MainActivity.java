package com.roadempire.game;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.view.*;
import java.util.Random;

public class MainActivity extends Activity {
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);setContentView(new Game(this));}
  class Game extends View {
    Canvas c; Paint p=new Paint(3); Random rand=new Random(42); SharedPreferences save;
    final String[] names={"Toyota Corolla","Kia Niro","Hyundai Elantra","Ford Mustang","Hummer H2","Ford Super Duty"};
    final int[] prices={0,2800,4800,8700,15000,22000}, tops={145,155,174,238,168,182};
    final int[] colors={0xffd8e4ed,0xff52bfd0,0xffc5d1f5,0xfff3c13d,0xffa4c989,0xffd44951};
    int owned=1, selected=0,coins=600,level=1,winCount=0,stage=0,gear=3; // P R N D
    boolean awd=false,hand=false,garage=false,racing=false,parked=false,finished=false;
    float speed=0,position=0,steer=0,progress=0,world=0,offset=0,target=850,opponent=0;
    boolean gas=false,brake=false,left=false,right=false;
    float W=1100,H=620,sx=1,sy=1; long last=System.nanoTime(); String toast="DRIVE TO EXPLORE";float toastT=3;
    Game(Context ctx){super(ctx);setLayerType(View.LAYER_TYPE_SOFTWARE,null);save=ctx.getSharedPreferences("road_empire",0);coins=save.getInt("coins",600);level=save.getInt("level",1);owned=save.getInt("owned",1);selected=save.getInt("selected",0);}
    void store(){save.edit().putInt("coins",coins).putInt("level",level).putInt("owned",owned).putInt("selected",selected).apply();}
    void msg(String s){toast=s;toastT=2.5f;}
    void box(float a,float b,float d,float e,int clr){p.setColor(clr);p.setStyle(Paint.Style.FILL);c.drawRect(a,b,d,e,p);}
    void round(float a,float b,float d,float e,float r,int clr){p.setColor(clr);p.setStyle(Paint.Style.FILL);c.drawRoundRect(a,b,d,e,r,r,p);}
    void circle(float x,float y,float r,int clr){p.setColor(clr);p.setStyle(Paint.Style.FILL);c.drawCircle(x,y,r,p);}
    void line(float x,float y,float xx,float yy,int clr,float sw){p.setColor(clr);p.setStrokeWidth(sw);p.setStyle(Paint.Style.STROKE);c.drawLine(x,y,xx,yy,p);p.setStyle(Paint.Style.FILL);}
    void txt(String s,float x,float y,int sz,int clr){p.setColor(clr);p.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));p.setTextSize(sz);p.setTextAlign(Paint.Align.CENTER);c.drawText(s,x,y,p);}
    void poly(int clr,float... pts){Path path=new Path();path.moveTo(pts[0],pts[1]);for(int i=2;i<pts.length;i+=2)path.lineTo(pts[i],pts[i+1]);path.close();p.setColor(clr);c.drawPath(path,p);}
    void update(float dt){
      toastT-=dt;
      float dir=(right?1:0)-(left?1:0);steer+=(dir-steer)*Math.min(1,dt*5);
      if(!garage){
        float acceleration=(stage==1&&!awd&&selected>=4?22:gas?58:0);
        float limit=tops[selected]*(stage==1&&!awd?0.52f:1f);
        if(gear==3&&!hand){speed+=((gas?acceleration:0)-(brake?110:0)-8-speed*.052f)*dt;speed=Math.max(0,Math.min(limit,speed));}
        else if(gear==1&&!hand){speed+=((gas?35:0)-(brake?95:0)-9-speed*.07f)*dt;speed=Math.max(0,Math.min(36,speed));}
        else speed=Math.max(0,speed-(hand?180:95)*dt);
        float sign=gear==1?-1:1;
        position+=steer*speed*dt*.037f*sign;
        position=Math.max(-3.2f,Math.min(3.2f,position));
        float bump=Math.abs(position)>1.7f?0.72f:1f;
        float moving=speed*dt/3.6f*bump;
        world+=moving*sign;
        if(racing){
          progress+=Math.max(0,moving*sign);
          opponent+=dt*(8.5f+level*1.65f);
          if(progress>=target){
            racing=false;finished=true;boolean win=progress>=opponent;int prize=win?(450+level*230):100;
            coins+=prize;if(win){level++;winCount++;}store();msg(win?"YOU WIN! +"+prize+" COINS":"FINISH! +100 COINS");
          }
        }
        parked=gear==0&&speed<1; 
      }
    }
    void sky(){
      box(0,0,1100,620,0xff80b9e6);
      box(0,195,1100,410,stage==1?0xff91a879:0xff739d88);
      circle(942,75,39,0xffffe1a4);
      for(int i=0;i<7;i++){float x=i*230-(world*.014f%230);poly(stage==1?0xff718879:0xff89a1a4,x-200,273,x+32,95+(i%3)*20,x+250,273);}
      if(stage==0)for(int i=0;i<12;i++){float x=i*113-(world*.12f%113);float h=35+i%4*14;box(x,215-h,x+74,274,0xffb2c3c7);for(int j=0;j<3;j++)box(x+10+j*20,226-h,x+20+j*20,238-h,0xff637f96);}
    }
    float horizon=270;
    float roadCenter(float z){return 550-position*120+ (float)Math.sin((world+z)*.0017)*z*.065f;}
    float roadWidth(float z){return 75+z*.58f;}
    void road(){
      sky();
      for(int k=0;k<30;k++){
        float z1=k*15,z2=(k+1)*15;
        float y1=horizon+z1*.79f,y2=horizon+z2*.79f;
        float cx1=roadCenter(z1),cx2=roadCenter(z2);
        float w1=roadWidth(z1),w2=roadWidth(z2);
        poly(stage==1?0xff9d937e:0xff777779,cx1-w1-18,y1,cx1+w1+18,y1,cx2+w2+23,y2,cx2-w2-23,y2);
        poly(stage==1?0xff795f4b:0xff343a44,cx1-w1,y1,cx1+w1,y1,cx2+w2,y2,cx2-w2,y2);
        if(stage==0){line(cx1-w1,y1,cx2-w2,y2,0xffebeee7,3);line(cx1+w1,y1,cx2+w2,y2,0xffebeee7,3);}
        if(((int)((world+z1)/42))%3==0 && stage==0){
          poly(0xfff9f2bb,cx1-2,y1,cx1+2,y1,cx2+5,y2,cx2-5,y2);
        }
      }
      for(int i=0;i<13;i++){
        float z=(i*83-world*.75f%83+83)%700;float y=horizon+z*.63f;float x=roadCenter(z)+(i%2==0?-1:1)*(roadWidth(z)+53);
        if(y>580)continue;
        float scale=.27f+z/500;
        if(stage==1){
          poly(0xff557d50,x-23*scale,y,x,y-78*scale,x+24*scale,y);
          box(x-3*scale,y-12*scale,x+3*scale,y+8*scale,0xff725b41);
        }else{
          box(x-15*scale,y-46*scale,x+15*scale,y,0xffaaa49d);
          box(x-12*scale,y-41*scale,x+12*scale,y-23*scale,0xff6b9ab4);
          box(x-4*scale,y+2*scale,x+4*scale,y+17*scale,0xff5e6258);
        }
      }
      for(int i=0;i<4;i++){
        float z=(i*210+140-world*(racing?1.2f:.65f)%840+840)%840;
        if(z<80||z>530)continue;
        float y=horizon+z*.75f,sc=.18f+z/450;
        float x=roadCenter(z)+(i%2==0?-1:1)*roadWidth(z)*.48f;
        smallCar(x,y,sc,i);
      }
    }
    void smallCar(float x,float y,float s,int i){
      int color=new int[]{0xffbf564b,0xffc6cfdb,0xff3e95bb,0xffe6ba58}[i];
      round(x-28*s,y-50*s,x+28*s,y+14*s,8*s,color);
      poly(0xff7fa8bf,x-19*s,y-38*s,x+19*s,y-38*s,x+16*s,y-16*s,x-16*s,y-16*s);
      box(x-28*s,y+2*s,x-18*s,y+11*s,0xffa72526);box(x+18*s,y+2*s,x+28*s,y+11*s,0xffa72526);
    }
    void playerCar(){
      float x=550+steer*22,y=457;
      p.setColor(0x77000000);c.drawOval(x-79,y+95,x+83,y+119,p);
      round(x-79,y+25,x+79,y+108,19,0xff161a20);
      round(x-72,y+18,x+72,y+97,18,colors[selected]);
      poly(0xff233f56,x-45,y+14,x+45,y+14,x+61,y+55,x-61,y+55);
      poly(0xff93bbd3,x-39,y+19,x+39,y+19,x+50,y+45,x-50,y+45);
      line(x-48,y+53,x+50,y+53,0xff1a2431,6);
      round(x-68,y+65,x-38,y+81,4,0xffe64c47);round(x+38,y+65,x+68,y+81,4,0xffe64c47);
      box(x-64,y+93,x-46,y+105,0xff262e37);box(x+46,y+93,x+64,y+105,0xff262e37);
      box(x-23,y+88,x+23,y+102,0xff1e2a35);
      txt(names[selected],x,y+8,17,0xff102436);
    }
    void button(String s,float x,float y,float w,float h,int clr){round(x,y,x+w,y+h,11,clr);txt(s,x+w/2,y+h/2+8,23,0xffffffff);}
    void hud(){
      round(12,12,301,92,12,0xe5192637);
      txt("ROAD EMPIRE",151,39,26,0xffffca50);
      txt("COINS "+coins+"    LEVEL "+level,151,70,20,0xfff1f5ff);
      round(323,12,785,81,13,0xe51a2838);
      txt((int)speed+" km/h",426,51,33,0xffe9faff);
      txt(new String[]{"P","R","N","D"}[gear],568,52,37,0xffffd068);
      txt(stage==0?"CITY":"MOUNTAIN",686,51,23,0xffa7e5e6);
      button("GARAGE",889,12,198,57,0xff4d5473);
      button(stage==0?"MOUNTAIN":"CITY",790,84,146,43,0xff384b62);
      button("RACE",943,84,145,43,0xff9b7335);
      if(racing){round(382,91,762,119,8,0xff183243);box(386,95,386+Math.min(370,progress/target*370),115,0xfff3c753);txt("RACE "+(int)(100*progress/target)+"%",572,113,16,0xffffffff);}
      button("4x4 "+(awd?"ON":"OFF"),13,108,127,47,awd?0xff429779:0xff475366);
      if(selected<4)txt("AWD: TRUCKS ONLY",235,136,16,0xfffff7c8);
      button("HANDBRAKE "+(hand?"ON":"OFF"),13,170,192,50,hand?0xffaa4d45:0xff40556a);
      txt("GEAR",55,270,19,0xfff0f5fa);
      for(int i=0;i<4;i++)button(new String[]{"P","R","N","D"}[i],13,278+i*56,72,50,i==gear?0xffcd9a42:0xff273747);
      // steering wheel, articulated direction indicator
      circle(202,485,99,0xaa11212d);p.setStyle(Paint.Style.STROKE);p.setColor(0xffdce8eb);p.setStrokeWidth(11);c.drawCircle(202,485,70,p);p.setStyle(Paint.Style.FILL);
      line(202,485,202+(float)Math.sin(steer)*64,485-(float)Math.cos(steer)*64,0xffdce8eb,11);
      line(202,485,140,513,0xffdce8eb,8);line(202,485,264,513,0xffdce8eb,8);circle(202,485,20,0xff748596);
      button("◀",86,553,92,52,0xff3b677d);button("▶",220,553,92,52,0xff3b677d);
      button("BRAKE",755,473,138,130,brake?0xffc84c41:0xff6a4543);
      button("GAS",912,416,175,187,gas?0xff3fa17d:0xff356d55);
      if(parked)txt("PARKED",550,420,39,0xffe0fff6);
      if(toastT>0){round(352,153,747,209,12,0xdd202b37);txt(toast,550,191,28,0xffffe3a7);}
    }
    void garage(){
      round(125,75,975,560,22,0xf11b2838);
      txt("GARAGE • BUY & SELECT CARS",550,120,33,0xffffcc6a);
      txt("BALANCE "+coins+" COINS",550,159,22,0xffe2eef6);
      for(int i=0;i<6;i++){
        int y=177+i*57;
        round(165,y,931,y+50,11,i==selected?0xff385773:0xff26394c);
        circle(194,y+24,12,colors[i]);
        p.setTextAlign(Paint.Align.LEFT);p.setTextSize(22);p.setColor(0xffffffff);c.drawText(names[i],218,y+32,p);
        txt(tops[i]+" km/h",635,y+31,19,0xffc9e0ec);
        boolean have=(owned&(1<<i))!=0;txt(have?(i==selected?"SELECTED":"OWNED"):prices[i]+" C",839,y+31,20,have?0xff91e4ba:0xffffc972);
      }
      button("CLOSE",440,535,215,65,0xff976447);
    }
    @Override protected void onDraw(Canvas canvas){
      float dt=Math.min(.045f,(System.nanoTime()-last)/1e9f);last=System.nanoTime();
      update(dt);c=canvas;sx=getWidth()/1100f;sy=getHeight()/620f;c.save();c.scale(sx,sy);
      road();playerCar();hud();if(garage)garage();c.restore();postInvalidateDelayed(16);
    }
    boolean in(float x,float y,float a,float b,float w,float h){return x>=a&&x<=a+w&&y>=b&&y<=b+h;}
    void down(float x,float y){
      if(garage){
        if(in(x,y,440,535,215,85)){garage=false;return;}
        for(int i=0;i<6;i++)if(in(x,y,165,177+i*57,766,50)){
          if((owned&(1<<i))!=0){selected=i;store();msg("SELECTED "+names[i]);}
          else if(coins>=prices[i]){coins-=prices[i];owned|=1<<i;selected=i;store();msg("PURCHASED!");}
          else msg("NOT ENOUGH COINS");
          return;
        }return;
      }
      if(in(x,y,889,12,198,57)){garage=true;gas=false;brake=false;return;}
      if(in(x,y,790,84,146,43)){stage=1-stage;position=0;speed=0;msg(stage==1?"MOUNTAIN TRAIL":"CITY STREETS");return;}
      if(in(x,y,943,84,145,43)){racing=true;finished=false;progress=0;opponent=0;target=550+level*190;gear=3;hand=false;msg("GO! BEAT THE RIVAL");return;}
      if(in(x,y,13,108,127,47)){if(selected>=4){awd=!awd;msg(awd?"4X4 ENGAGED":"4X4 DISENGAGED");}else msg("4X4 FOR TRUCKS & HUMMER");return;}
      if(in(x,y,13,170,192,50)){hand=!hand;msg(hand?"PARKING BRAKE ON":"PARKING BRAKE OFF");return;}
      for(int i=0;i<4;i++)if(in(x,y,13,278+i*56,72,50)){if(speed<5){gear=i;msg("GEAR "+new String[]{"P","R","N","D"}[i]);}else msg("STOP TO SHIFT");return;}
      if(in(x,y,75,545,112,70))left=true;
      if(in(x,y,218,545,110,70))right=true;
      if(in(x,y,748,468,160,143))brake=true;
      if(in(x,y,905,409,193,203))gas=true;
    }
    @Override public boolean onTouchEvent(android.view.MotionEvent e){
      int action=e.getActionMasked(),idx=e.getActionIndex();
      if(action==MotionEvent.ACTION_DOWN||action==MotionEvent.ACTION_POINTER_DOWN){down(e.getX(idx)/sx,e.getY(idx)/sy);return true;}
      if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL){gas=false;brake=false;left=false;right=false;return true;}
      if(action==MotionEvent.ACTION_POINTER_UP){float x=e.getX(idx)/sx,y=e.getY(idx)/sy;if(in(x,y,75,545,112,70))left=false;if(in(x,y,218,545,110,70))right=false;if(in(x,y,748,468,160,143))brake=false;if(in(x,y,905,409,193,203))gas=false;return true;}
      return true;
    }
  }
}