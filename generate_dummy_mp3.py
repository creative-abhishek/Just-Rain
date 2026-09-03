import struct
with open('dummy.mp3', 'wb') as f:
    # write a minimum valid mp3 frame or just some junk
    f.write(b'\xFF\xFB\x90\x44\x00\x00\x00\x00\x00\x00\x00\x00')
