package audiolizer;

import Box.Token.TokenTypeEnum;

//InterpreterAudio.java
public interface InterpreterAudio {
 void onEvent(AudioEvent e);
  record AudioEvent(TokenTypeEnum type, String lexeme, double value, long tNanos) {
     
 }

 // No-op implementation for when audio is disabled
 InterpreterAudio NONE = e -> {};
}
