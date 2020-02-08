import socket
from drawnow import drawnow, figure
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import datetime
from imu2xyz import imu2V, imu2X, p2e
import numpy as np

def parse_stream(line):
    STREAM_ID = {
        '3': 'acc', # accelerometer
        '4': 'gy', # gyroscope
        '5': 'm', # magnetic field
        '81': 'o', # orientation (sensor fusion)
        '82': 'a', # linear acceleration (sensor fusion)
        '84': 'r', # rotation vector (sensor fusion)
    }
    dat = line.split(b',')
    sensor_data = {}
    sensor_data['t'] = float(dat[0])
    for i in range(1,len(dat),4):
        key = dat[i].decode('ascii').strip()
        var = STREAM_ID[key]
        # sensor_data[var] = [float(x) for x in dat[i+1:i+4]]
        sensor_data[var] = [float(dat[i+2]), float(dat[i+3]),float(dat[i+1])] # order is z,x,y
    return sensor_data

UDP_IP = "192.168.0.122"
UDP_PORT = 5555

sock = socket.socket(socket.AF_INET, # Internet
                     socket.SOCK_DGRAM) # UDP
sock.bind((UDP_IP, UDP_PORT))

figure(figsize=(7, 7/2))
fig = plt.gcf()

# x=[]
# t=[]

xx = yy = zz = 0
def draw_fig():
    ax = fig.add_subplot(111, projection='3d')
    # ax.set_xlim(-100,100)
    # ax.set_ylim(-100,100)
    # ax.set_zlim(-100,100)
    ax.set_xlim(-1,1)
    ax.set_ylim(-1,1)
    ax.set_zlim(-1,1)

    ax.set_xlabel('x')
    ax.set_ylabel('y')
    ax.set_zlabel('z')

    ax.plot([0,xx],[0,yy],[0,zz])
    ax.plot([0,xx],[0,0],[0,0],'grey')
    ax.plot([0,0],[0,yy],[0,0],'grey')
    ax.plot([0,0],[0,0],[0,zz],'grey')
    plt.show()

A=[]
O=[]
T=[]

acq_start = False
while True:
    try:
        line, addr = sock.recvfrom(1024) # buffer size is 1024 bytes
    except socket.timeout:
        break
    if not acq_start:
        acq_start = True
        sock.settimeout(1)
    last_acq = datetime.datetime.now()
    # print("received message:", line)
    # dat = data.split(b',')
    parsed_data = parse_stream(line)
    if 'o' in parsed_data.keys() and 'a' in parsed_data.keys() :
        A.append(parsed_data['a'])
        O.append(parsed_data['o'])
        T.append(parsed_data['t'])
        print(parsed_data)
        # xx = parsed_data['r'][0]
        # yy = parsed_data['r'][1]
        # zz = parsed_data['r'][2]
        aa = parsed_data['a']
        rr = parsed_data['o']
        dx,dy,dz = p2e(aa,rr)
        if len(T)>2:
            xx += dx * (T[-1]-T[-2])
            yy += dy * (T[-1]-T[-2]) 
            zz += dz * (T[-1]-T[-2])
            drawnow(draw_fig)

    # t.append(float(dat[0]))
    # x.append(float(dat[2]))

# print(T)
# print(A)
# print(O)

V = imu2V(T, A, O)
X = imu2X(T, A, O)

print(X.shape) 
fig = plt.figure()
plt.plot(X[:,0],X[:,1],'-x')
plt.savefig('xy.png')

fig = plt.figure()
plt.plot(V[:,0],V[:,1],'-x')
plt.savefig('VxVy.png')