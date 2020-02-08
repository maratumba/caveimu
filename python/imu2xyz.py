import numpy as np
from scipy.integrate import cumtrapz

def rot_mat(a,b,t, deg=True): # z,y,x
# def rot_mat(a,b,t, deg=True):
# t = z, b=y, a=x

  if deg:
    a = np.deg2rad(a)
    b = np.deg2rad(b)
    t = np.deg2rad(t)

  R11 = np.cos(t) * np.cos(b)
  R12 = - np.sin(t) * np.cos(a) + np.cos(t) * np.sin(b) * np.sin(a)
  R13 = np.sin(t) * np.sin(a) + np.cos(t) * np.sin(b) * np.cos(a)
  R21 = np.sin(t) * np.cos(b)
  R22 = np.cos(t) * np.cos(a) + np.sin(t) * np.sin(b) * np.sin(a)
  R23 = -np.cos(t) * np.sin(a) + np.sin(t) * np.sin(b) * np.cos(a)
  R31 = -np.sin(b)
  R32 = np.cos(b) * np.sin(a)
  R33 = np.cos(b) * np.cos(a)

  R = [
    [R11, R12, R13],
    [R21, R22, R23],
    [R31, R32, R33],
  ]i

  return np.array(R)

def p2e(v,r):
  R = rot_mat(*r)
  return np.matmul(R.T,v)


def fixEnd(v4, v3_end):
  # assume order t,x,y,z for v4
  v4 = np.array(v4)
  v3_start = v4[0][1:]
  v3_end = np.array(v3_end)
  T,X,Y,Z = v4[:,0],v4[:,1],v4[:,2],v4[:,3]

  v3_error = v3_end - v3_start
  d_error = v3_error / len(T)

  X_fixed = (X + d_error)
  Y_fixed = (Y + d_error)
  Z_fixed = (Z + d_error)
  v4_fixed = np.array([T,X_fixed,Y_fixed,Z_fixed])
  return v4_fixed


def imu2V(T,A,O):
  # O = [[azimuth (z), pitch (x), roll (y)]]
  # A = 

  x0 = y0 = z0 = 0
  t0 = T[0] 
  v0 = 0
  A_e = []
  # x = 1/2 a dt^2
  for i in range(len(A)):
    a_p = A[i]
    r = O[i]
    a_e = p2e(a_p, r)
    A_e.append(a_e)
  
  A_e = np.vstack(A_e)
  V_e = cumtrapz(A_e, T, axis=0)

  return V_e

def imu2X(T,A,O):
  V_e = imu2V(T,A,O)
  X_e = cumtrapz(V_e, T[:-1], axis=0)

  return X_e
