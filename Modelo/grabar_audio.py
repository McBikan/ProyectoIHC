import sounddevice as sd
from scipy.io.wavfile import write  


def grabar(nombre="audio_usuario.wav", duracion=3, fs=16000):
    print("Grabando...")
    audio = sd.rec(int(duracion * fs), samplerate=fs, channels=1, dtype='int16')
    sd.wait()
    write(nombre, fs, audio)
    print(f"Grabación guardada como {nombre}")
    
if __name__ == "__main__":
    grabar(duracion=3)