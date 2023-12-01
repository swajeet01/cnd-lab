import express from 'express'
import { createServer } from 'node:http'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'
import { Server } from 'socket.io'

const app = express()
const server = createServer(app)
const io = new Server(server)

const currentDir = dirname(fileURLToPath(import.meta.url))

app.get('/', (req, res) => {
  res.sendFile(join(currentDir, 'index.html'))
})

io.on('connection', (socket) => {
  console.log('A new user connected.')
  socket.on('disconnect', () => {
    console.log('A user disconnect.')
  })
  socket.on('chat-message', (msg) => {
    io.emit('chat-message', msg)
  })
})

const PORT = 3000
server.listen(PORT, () => {
  console.log(`Server is listening on port ${PORT}`)
})
