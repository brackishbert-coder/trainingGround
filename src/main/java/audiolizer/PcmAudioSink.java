package audiolizer;

import javax.sound.sampled.*;

import Box.Token.TokenType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public final class PcmAudioSink implements InterpreterAudio, AutoCloseable {
    private final SourceDataLine line;
    private final AudioFormat fmt;
    private final ScheduledExecutorService sch =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "PcmAudioSched");
            t.setPriority(Thread.NORM_PRIORITY);
            t.setDaemon(true);
            return t;
        });

    public PcmAudioSink() {
        AudioFormat[] formats = new AudioFormat[] {
            new AudioFormat(48000, 16, 2, true, false),
            new AudioFormat(44100, 16, 2, true, false),
            new AudioFormat(48000, 16, 1, true, false),
            new AudioFormat(44100, 16, 1, true, false),
        };

        // Try default mixer first, then all mixers
        List<Mixer.Info> mixers = new ArrayList<>();
        Mixer.Info def = AudioSystem.getMixerInfo().length > 0 ? AudioSystem.getMixerInfo()[0] : null;
        if (def != null) mixers.add(def);
        for (Mixer.Info mi : AudioSystem.getMixerInfo()) {
            if (def == null || !mi.equals(def)) mixers.add(mi);
        }

        SourceDataLine gotLine = null;
        AudioFormat gotFmt = null;
        StringBuilder tried = new StringBuilder();

        outer:
        for (Mixer.Info mi : mixers) {
            Mixer m = AudioSystem.getMixer(mi);
            for (AudioFormat f : formats) {
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, f);
                boolean sup = m.isLineSupported(info) || AudioSystem.isLineSupported(info);
                tried.append("Mixer=").append(mi.getName()).append(" fmt=").append(f).append(" sup=").append(sup).append('\n');
                if (!sup) continue;
                try {
                    gotLine = (SourceDataLine) m.getLine(info);
                    // ~100ms buffer or min 4k
                    int buf = Math.max(4096, (int)(f.getFrameRate() * f.getFrameSize() / 10));
                    gotLine.open(f, buf);
                    gotFmt = f;
                    break outer;
                } catch (Exception openFail) {
                    if (gotLine != null) try { gotLine.close(); } catch (Exception ignored) {}
                    gotLine = null;
                }
            }
        }

        if (gotLine == null || gotFmt == null) {
            throw new RuntimeException("No compatible PCM line found. Tried:\n" + tried);
        }

        fmt = gotFmt;
        line = gotLine;
        line.start();
        System.out.println("[PCM] using mixer");
    }

    @Override public void onEvent(AudioEvent e) {
        switch (e.type()) {
            case TokenType.PRINT -> tone(220 + (Math.floorMod(e.lexeme().hashCode(), 12) * 22), 80);
            case TokenType.TNIRP -> tone(220 + (int)Math.round(e.value() * 600), 100);
            case TokenType.PLUS -> tone(440, 90);
            case TokenType.MINUS -> tone(430, 80);
            case TokenType.FORWARDSLASH  -> tone(462, 80);
            case TokenType.TIMES -> tone(410, 140);
		default -> throw new IllegalArgumentException("Unexpected value: " + e.type());
        }
    }

    private void tone(int hz, int lenMs) {
        int frames = (int)((lenMs / 1000.0) * fmt.getSampleRate());
        int channels = fmt.getChannels();
        byte[] buf = new byte[frames * fmt.getFrameSize()];
        double sr = fmt.getSampleRate();

        for (int i = 0; i < frames; i++) {
            double s = Math.sin(2 * Math.PI * hz * i / sr);
            short v = (short)(s * 2800); // keep it quiet
            int idx = i * fmt.getFrameSize();
            // little-endian 16-bit
            for (int ch = 0; ch < channels; ch++) {
                buf[idx + ch*2]     = (byte)(v & 0xFF);
                buf[idx + ch*2 + 1] = (byte)((v >>> 8) & 0xFF);
            }
        }
        sch.execute(() -> line.write(buf, 0, buf.length));
    }

    @Override public void close() {
        sch.shutdownNow();
        try { line.drain(); } catch (Exception ignored) {}
        try { line.stop(); } catch (Exception ignored) {}
        try { line.close(); } catch (Exception ignored) {}
    }


}
