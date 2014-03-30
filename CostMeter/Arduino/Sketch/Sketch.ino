#include <RCSwitch.h>
#define NUM_SIGNALS 1
/* Mask Variables */
int device1 = 1;
int device2 = 2;
int device3 = 4;
int dataRead = 8;
/* RF Codes */
long on1 = 4478259;
long off1 = 4478268;
long on2 = 4478403;
long off2 = 4478412;
long on3 = 4478723;
long off3 = 4478732;
//Incoming data
int incomingByte = 0;
RCSwitch transmitter = RCSwitch();
/* Send a RF message */
void sendMessage(long message)
{
  for(int i=0;i<NUM_SIGNALS;i++)
    transmitter.send(message,24);
  
}
void setup(){
 Serial.begin(9600); 
 transmitter.enableTransmit(10);
 transmitter.setProtocol(1,180);
}
void loop(){
  if(Serial.available()>0)
  {
   incomingByte = Serial.read();
   //Serial.println(incomingByte);
   if (incomingByte & 4) {
     sendMessage(on1);
   } else {
     sendMessage(off1);
   }
   
   if (incomingByte & 2) {
      sendMessage(on2);
   } else {
      sendMessage(off2);
   }
   
   if (incomingByte & 1) {
      sendMessage(on3);
   } else {
      sendMessage(off3);
   }
  }
}

