#include "Timer.h"

#define relay1 2

#define relay2 3

String voice;



int Q1=10,Q2=11,Q3=12,Q4=13; // connected to arduino for motor


void setup() 
{

  Serial.begin(9600);

  
  
  pinMode(relay1,OUTPUT);

  pinMode(relay2,OUTPUT);

  digitalWrite(relay1,HIGH);

  digitalWrite(relay2,HIGH);

  pinMode(Q1,OUTPUT);
  pinMode(Q2,OUTPUT);
  pinMode(Q3,OUTPUT);
  pinMode(Q4,OUTPUT);
  
  digitalWrite(Q1,HIGH);
  digitalWrite(Q2,HIGH);
  digitalWrite(Q3,HIGH);
  digitalWrite(Q4,HIGH);
}

void loop() 
{

  while(Serial.available())

    {
  
      delay(10);

      char c = Serial.read();

      if(c=='#')
      {

        break;

      }

      voice +=c;

  }

if(voice.length()>0)

{

  Serial.println(voice);

  if(voice=="*light on"){

  lighton();

}

else if(voice=="*light off")
  {

    lightoff();

  }

else if(voice=="*fan on")
  {

    fanon();

  }

else if(voice=="*fan stop")
  {

    fanoff();

  }
else if(voice=="*open curtain")
  {

    curtainopen();

  }
else if(voice=="*close curtain")
  {

    curtainclose();

  }
else if(voice=="*switch off everything")
  {
    lightoff();
      fanoff();
    curtainclose();

  }
    voice="";

}

}





void lighton()

{

  digitalWrite(relay1,LOW);

}

void lightoff()

{

  digitalWrite(relay1,HIGH);

}



void fanon()

{

  digitalWrite(relay2,LOW);

}

void fanoff()

{

  digitalWrite(relay2,HIGH);

}

void curtainopen()
{
  
    
  Serial.println("");
  Serial.println("Open");
  digitalWrite(Q1,HIGH);
  digitalWrite(Q2,LOW);
  digitalWrite(Q3,LOW);
  digitalWrite(Q4,HIGH);
  Serial.println("Open"); 
  
 
}

void curtainclose()

{
  Serial.println("");
  Serial.println("Close");
  digitalWrite(Q1,LOW);
  digitalWrite(Q2,HIGH);
  digitalWrite(Q3,HIGH);
  digitalWrite(Q4,LOW); 
}
